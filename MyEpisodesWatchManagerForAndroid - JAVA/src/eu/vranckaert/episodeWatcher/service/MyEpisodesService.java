package eu.vranckaert.episodeWatcher.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.domain.FeedItem;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.FeedUrlBuildingFaildException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.PasswordEnctyptionFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;

public class MyEpisodesService {
	private static final String LOG_TAG = MyEpisodesService.class.getName();
	
    private static final String UID_REPLACEMENT_STRING = "[UID]";
    private static final String PWD_REPLACEMENT_STRING = "[PWD]";
    private static final String FEED = "unwatched";
    private static final String SHOW_IGNORED = "0";
    private static final String UNWATCHED_EPISODES_URL = "http://www.myepisodes.com/rss.php" +
                                                            "?feed=" + FEED +
                                                            "&showignored=" + SHOW_IGNORED +
                                                            "&uid=" + UID_REPLACEMENT_STRING +
                                                            "&pwdmd5=" + PWD_REPLACEMENT_STRING;
    private static final String PASSWORD_ENCRYPTION_TYPE = "MD5";
    private static final String FEED_TITLE_SEPERATOR = " \\]\\[ ";
    private static final String SEASON_EPISODE_NUMBER_SEPERATOR = "x";
    private static final int FEED_TITLE_EPISODE_FIELDS = 4;
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
    private static final String MYEPISODES_LOGIN_PAGE = "http://www.myepisodes.com/login.php";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_USERNAME = "username";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD = "password";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_ACTION = "action";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE = "Login";
    private static final String MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT = "[ID]";
    private static final String MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT = "[S]";
    private static final String MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT = "[E]";
    private static final int MYEPISODES_UPDATE_PAGE_SEEN = 1;
    private static final String MYEPISODES_UPDATE_PAGE = "http://www.myepisodes.com/myshows.php?action=Update" +
                                                            "&showid=" + MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT +
                                                            "&season=" + MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT +
                                                            "&episode=" + MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT +
                                                            "&seen=" + MYEPISODES_UPDATE_PAGE_SEEN;

	private static final String HTTP_POST_ENCODING = HTTP.UTF_8;

    public List<Episode> retrieveEpisodes(final User user) throws InternetConnectivityException, Exception {
        String encryptedPassword = encryptPassword(user.getPassword());
        URL feedUrl = buildUnwatchedEpisodesUrl(user.getUsername(), encryptedPassword);
        RssFeedParser rssFeedParser = new SaxRssFeedParser();
        Feed rssFeed;
		rssFeed = rssFeedParser.parseFeed(feedUrl);

        List<Episode> episodes = new ArrayList<Episode>(0);

        for (FeedItem item : rssFeed.getItems()) {
            Episode episode = new Episode();

            StringBuilder title = new StringBuilder(item.getTitle());
            
            if (title.length() > 0) {
	            //Sample title: [ Reaper ][ 01x14 ][ Rebellion ][ 23-Apr-2008 ]
	            title = title.replace(0, 2, ""); //Strip off first bracket [
	            title = title.replace(title.length()-2, title.length(), ""); //Strip off last bracket ]
	            String[] episodeInfo = title.toString().split(FEED_TITLE_SEPERATOR);
	            if (episodeInfo.length == FEED_TITLE_EPISODE_FIELDS) {
	                episode.setShowName(episodeInfo[0].trim());
	                getSeasonAndEpisodeNumber(episodeInfo[1], episode);
	                episode.setName(episodeInfo[2].trim());
	                episode.setAirDate(parseDate(episodeInfo[3].trim()));
	                episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
	                episode.setId();
	                
	                episodes.add(episode);
	            } else if (episodeInfo.length == FEED_TITLE_EPISODE_FIELDS - 1) {
	            	//Solves problem mentioned in Issue 20
	            	episode.setShowName(episodeInfo[0].trim());
	                getSeasonAndEpisodeNumber(episodeInfo[1], episode);
	                episode.setName(episodeInfo[2].trim() + "...");
	                episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
	                episode.setId();
	                
	                episodes.add(episode);
	            } else {
	                String message = "Problem parsing a feed item. Feed details: " + item.toString();
	                Log.e(LOG_TAG, message);
	            }
            }
        }

        return episodes;
    }

    public void watchedEpisode(Episode episode, User user) throws LoginFailedException
            , ShowUpdateFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        HttpClient httpClient = new DefaultHttpClient();

        login(httpClient, user.getUsername(), user.getPassword());
        markAsSeen(httpClient, episode);
        
        httpClient.getConnectionManager().shutdown();
    }

    private void markAsSeen(HttpClient httpClient, Episode episode) throws ShowUpdateFailedException, InternetConnectivityException {
        String urlRep = MYEPISODES_UPDATE_PAGE;
        urlRep = urlRep.replace(MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT, String.valueOf(episode.getEpisode()));
        urlRep = urlRep.replace(MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT, String.valueOf(episode.getSeason()));
        urlRep = urlRep.replace(MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT, episode.getMyEpisodeID());

        HttpGet get = new HttpGet(urlRep);

        int status = 200;

        try {
        	HttpResponse response = httpClient.execute(get);
        	status = response.getStatusLine().getStatusCode();
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Updating the show status failed for URL " + urlRep;
            Log.w(LOG_TAG, message, e);
            throw new ShowUpdateFailedException(message, e);
        }

        if (status != 200) {
            String message = "Updating the show status failed with status code " + status + " for URL " + urlRep;
            Log.w(LOG_TAG, message);
            throw new ShowUpdateFailedException(message);
        } else {
            Log.i(LOG_TAG, "Successfully updated the show from url " + urlRep + " (" + episode + ")");
        }
    }

    public boolean login(User user) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        HttpClient httpClient = new DefaultHttpClient();
    	boolean status = login(httpClient, user.getUsername(), user.getPassword());
        httpClient.getConnectionManager().shutdown();
        return status;
    }
    
    private boolean login(HttpClient httpClient, String username, String password) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	HttpPost post = new HttpPost(MYEPISODES_LOGIN_PAGE);

    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MYEPISODES_LOGIN_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MYEPISODES_LOGIN_PAGE_PARAM_ACTION, MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE));

        try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			String message = "Could not start logon because the HTTP post encoding is not supported";
			Log.e(LOG_TAG, message, e);
			throw new UnsupportedHttpPostEncodingException(message, e);
		}
		
		boolean result = false;
		String responsePage = "";
        HttpResponse response;
        try {
            response = httpClient.execute(post);
            responsePage = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Login to MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new LoginFailedException(message, e);
        }

        if (responsePage.contains("Wrong username/password")) {
            String message = "Login to MyEpisodes failed. Login page: " + MYEPISODES_LOGIN_PAGE + " Username: " + username +
            					" Password: ***** Leaving with status code " + result;
            Log.w(LOG_TAG, message);
            throw new LoginFailedException(message);
        } else {
            Log.i(LOG_TAG, "Successfull login to " + MYEPISODES_LOGIN_PAGE + result);
            result = true;
        }
        return result;
    }

    private Date parseDate(String date) {
        Log.d(LOG_TAG, "Airing date from feed: " + date);
        try {
            return DATEFORMAT.parse(date);
        } catch (ParseException e) {
            Log.i(LOG_TAG, "Date could not be parsed, using default date", e);
            return new Date();
        }
    }

    private void getSeasonAndEpisodeNumber(String seasonEpisodeNumber, Episode episode) {
        String[] episodeInfoNumber = seasonEpisodeNumber.split(SEASON_EPISODE_NUMBER_SEPERATOR);
        episode.setSeason(Integer.parseInt(episodeInfoNumber[0].trim()));
        episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));;
    }

    private URL buildUnwatchedEpisodesUrl(final String username, final String encryptedPassword) throws FeedUrlBuildingFaildException {
        String urlRep = UNWATCHED_EPISODES_URL;
        urlRep = urlRep.replace(UID_REPLACEMENT_STRING, username);
        urlRep = urlRep.replace(PWD_REPLACEMENT_STRING, encryptedPassword);

        URL url = null;
        try {
            url = new URL(urlRep);
        } catch (MalformedURLException e) {
            String message = "The feed URL could not be build";
            Log.e(LOG_TAG, message, e);
            throw new FeedUrlBuildingFaildException(message, e);
        }

        Log.d(LOG_TAG, "FEED URL: " + url);

        return url;
    }

    private String encryptPassword(final String password) throws PasswordEnctyptionFailedException {
        String encryptedPwd = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(PASSWORD_ENCRYPTION_TYPE);
            digest.update(password.getBytes());
            BigInteger hash = new BigInteger(1, digest.digest());
            encryptedPwd = hash.toString(16);
        } catch (NoSuchAlgorithmException e) {
            String message = "The password could not be encrypted because there is no such algorithm (" + PASSWORD_ENCRYPTION_TYPE + ")";
            Log.e(LOG_TAG, message, e);
            throw new PasswordEnctyptionFailedException(message, e);
        }
        return encryptedPwd;
    }
}
