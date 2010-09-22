package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import eu.vranckaert.episodeWatcher.domain.*;
import eu.vranckaert.episodeWatcher.exception.*;
import eu.vranckaert.episodeWatcher.utils.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.pojava.datetime.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private static final String UNAQUIRED_EPISODES_URL = "http://www.myepisodes.com/rss.php?feed=unacquired" +
    														"&showignored=" + SHOW_IGNORED +
															"&uid=" + UID_REPLACEMENT_STRING +
															"&pwdmd5=" + PWD_REPLACEMENT_STRING;
    private static final String COMING_EPISODES_URL = "http://www.myepisodes.com/rss.php?feed=mylist" +
    														"&showignored=" + SHOW_IGNORED +
    														"&onlyunacquired=1" +
															"&uid=" + UID_REPLACEMENT_STRING +
															"&pwdmd5=" + PWD_REPLACEMENT_STRING;  
    private static final String PASSWORD_ENCRYPTION_TYPE = "MD5";
    private static final String FEED_TITLE_SEPERATOR = " \\]\\[ ";
    private static final String SEASON_EPISODE_NUMBER_SEPERATOR = "x";
    private static final int FEED_TITLE_EPISODE_FIELDS = 4;
    private static final String MYEPISODES_FORM_PARAM_ACTION = "action";
    private static final String MYEPISODES_REGISTER_PAGE = "http://www.myepisodes.com/register.php";
    private static final String MYEPISODES_REGISTER_PAGE_PARAM_USERNAME = "username";
    private static final String MYEPISODES_REGISTER_PAGE_PARAM_PASSWORD = "password";
    private static final String MYEPISODES_REGISTER_PAGE_PARAM_EMAIL = "user_email";
    private static final String MYEPISODES_REGISTER_PAGE_PARAM_ACTION_VALUE = "Register";
    private static final String MYEPISODES_LOGIN_PAGE = "http://www.myepisodes.com/login.php";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_USERNAME = "username";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD = "password";
    private static final String MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE = "Login";
    private static final String MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT = "[ID]";
    private static final String MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT = "[S]";
    private static final String MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT = "[E]";
    private static final int MYEPISODES_UPDATE_PAGE_SEEN = 1;
    private static final String MYEPISODES_UPDATE_WATCH = "http://www.myepisodes.com/myshows.php?action=Update" +
                                                            "&showid=" + MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT +
                                                            "&season=" + MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT +
                                                            "&episode=" + MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT +
                                                            "&seen=" + MYEPISODES_UPDATE_PAGE_SEEN;
    private static final int MYEPISODES_UPDATE_PAGE_UNSEEN = 0;
    private static final String MYEPISODES_UPDATE_ACQUIRE = "http://www.myepisodes.com/myshows.php?action=Update" +
    														"&showid=" + MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT +
    														"&season=" + MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT +
    														"&episode=" + MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT +
    														"&seen=" + MYEPISODES_UPDATE_PAGE_UNSEEN;

    private static final String MYEPISODES_SEARCH_PAGE = "http://www.myepisodes.com/search.php";
    private static final String MYEPISODES_SEARCH_PAGE_PARAM_SHOW = "tvshow";
    private static final String MYEPISODES_SEARCH_PAGE_PARAM_ACTION_VALUE = "Search myepisodes.com";
    private static final String MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_SEARCH_RESULTS = "Search results:";
    private static final String MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TABLE_END_TAG = "</table>";
    private static final String MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TD_START_TAG = "<td width=\"50%\"><a ";
    private static final String MYEPISODES_FAVO_IGNORE_PAGE = "http://myepisodes.com/shows.php";
	
    public List<Episode> retrieveEpisodes(int episodesType,final User user) throws InternetConnectivityException, Exception {
        String encryptedPassword = encryptPassword(user.getPassword());
        URL feedUrl = buildEpisodesUrl(episodesType, user.getUsername(), encryptedPassword);
        
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
	                
	                Log.d(LOG_TAG, "Episode from feed: " + episode.getShowName() + " - S" + episode.getSeasonString() + "E" + episode.getEpisodeString());
	                
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
        markAnEpisode(0, httpClient, episode);
        
        httpClient.getConnectionManager().shutdown();
    }
    
    public void acquireEpisode(Episode episode, User user) throws LoginFailedException
		    , ShowUpdateFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
		HttpClient httpClient = new DefaultHttpClient();
		
		login(httpClient, user.getUsername(), user.getPassword());
		markAnEpisode(1, httpClient, episode);
		
		httpClient.getConnectionManager().shutdown();
		}
    
    private void markAnEpisode(int EpisodeStatus, HttpClient httpClient, Episode episode) throws ShowUpdateFailedException, InternetConnectivityException {
    	String urlRep = "";
    	if(EpisodeStatus == 0)
        {
        	urlRep = MYEPISODES_UPDATE_WATCH;
        }
        else if(EpisodeStatus == 1)
        {
        	urlRep = MYEPISODES_UPDATE_ACQUIRE;
        }
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
    
    public boolean register(User user, String email) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        HttpClient httpClient = new DefaultHttpClient();
    	boolean status = false;
		try {
			status = RegisterUser(httpClient, user.getUsername(), user.getPassword(), email);
		} catch (RegisterFailedException e) {
			e.printStackTrace();
		}
        httpClient.getConnectionManager().shutdown();
        return status;
    }
    
    private boolean login(HttpClient httpClient, String username, String password) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	HttpPost post = new HttpPost(MYEPISODES_LOGIN_PAGE);

    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MYEPISODES_LOGIN_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MYEPISODES_LOGIN_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MYEPISODES_FORM_PARAM_ACTION, MYEPISODES_LOGIN_PAGE_PARAM_ACTION_VALUE));

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
            Log.i(LOG_TAG, "Successfull login to " + MYEPISODES_LOGIN_PAGE);
            result = true;
        }
        return result;
    }
    
    private boolean RegisterUser(HttpClient httpClient, String username, String password, String email) throws RegisterFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	HttpPost post = new HttpPost(MYEPISODES_REGISTER_PAGE);

    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MYEPISODES_REGISTER_PAGE_PARAM_USERNAME, username));
        nvps.add(new BasicNameValuePair(MYEPISODES_REGISTER_PAGE_PARAM_PASSWORD, password));
        nvps.add(new BasicNameValuePair(MYEPISODES_REGISTER_PAGE_PARAM_EMAIL, email));
        nvps.add(new BasicNameValuePair(MYEPISODES_FORM_PARAM_ACTION, MYEPISODES_REGISTER_PAGE_PARAM_ACTION_VALUE));

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
            throw new RegisterFailedException(message, e);
        }

        if (responsePage.contains("Username already exists, please choose another username.")) {
            String message = "Username already exists!";
            Log.w(LOG_TAG, message);
            throw new RegisterFailedException(message);
        } else {
            Log.i(LOG_TAG, "User succesfully created in. " + MYEPISODES_REGISTER_PAGE);
            result = true;
        }
        return result;
    }

    private Date parseDate(String date) {
        DateTime parsedDate = new DateTime(date);
        return parsedDate.toDate();
    }

    private void getSeasonAndEpisodeNumber(String seasonEpisodeNumber, Episode episode) {
    	if (seasonEpisodeNumber.startsWith("S"))
    	{
    		String[] episodeInfoNumber = seasonEpisodeNumber.split("E");
    		episode.setSeason(Integer.parseInt(episodeInfoNumber[0].replace("S", "").trim()));
    		episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));
    	}
    	else
    	{
	        String[] episodeInfoNumber = seasonEpisodeNumber.split(SEASON_EPISODE_NUMBER_SEPERATOR);
	        episode.setSeason(Integer.parseInt(episodeInfoNumber[0].trim()));
	        episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));
    	}
    }
    
    private URL buildEpisodesUrl(int episodesType,final String username, final String encryptedPassword) throws FeedUrlBuildingFaildException {
    	String urlRep = "";
    	switch(episodesType)
        {
	        case 0: urlRep = UNWATCHED_EPISODES_URL;
	        break;
	        case 1: urlRep = UNAQUIRED_EPISODES_URL;
	        break;  
	        case 2: urlRep = COMING_EPISODES_URL;
	        break; 
        }
    	
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
            digest.reset();
            digest.update(password.getBytes());

            byte[] messageDigest = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++) {
                hexString.append(String.format("%02x", messageDigest[i]));
            }
            encryptedPwd = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            String message = "The password could not be encrypted because there is no such algorithm (" + PASSWORD_ENCRYPTION_TYPE + ")";
            Log.e(LOG_TAG, message, e);
            throw new PasswordEnctyptionFailedException(message, e);
        }
        Log.d(LOG_TAG, "The encrypted password is " + encryptedPwd);
        return encryptedPwd;
    }

    public List<Show> searchShows(String search, User user) throws UnsupportedHttpPostEncodingException, InternetConnectivityException, LoginFailedException {
        HttpClient httpClient = new DefaultHttpClient();
        String username = user.getUsername();
        login(httpClient, username, user.getPassword());

    	HttpPost post = new HttpPost(MYEPISODES_SEARCH_PAGE);

    	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair(MYEPISODES_SEARCH_PAGE_PARAM_SHOW, search));
        nvps.add(new BasicNameValuePair(MYEPISODES_FORM_PARAM_ACTION, MYEPISODES_SEARCH_PAGE_PARAM_ACTION_VALUE));

        try {
			post.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e) {
			String message = "Could not start search because the HTTP post encoding is not supported";
			Log.e(LOG_TAG, message, e);
			throw new UnsupportedHttpPostEncodingException(message, e);
		}

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
            String message = "Search on MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new LoginFailedException(message, e);
        }

        List<Show> shows = extractSearchResults(responsePage);

        Log.d(LOG_TAG, shows.size() + " shows found for search value " + search);

        return shows;
    }

    /**
     * Extract a list of shows from the MyEpisodes.com HTML output!
     * @param html The MyEpisodes.com HTML output
     * @return A List of {@link eu.vranckaert.episodeWatcher.domain.Show} instances.
     */
    private List<Show> extractSearchResults(String html) {
        List<Show> shows = new ArrayList<Show>();
        if(html.contains("No results found.")) {
            return shows;
        }
        String[] split = html.split(MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_SEARCH_RESULTS);
        if(split.length == 2) {
            split = split[1].split(MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TABLE_END_TAG);
            if(split.length > 0) {
                split = split[0].split(MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TD_START_TAG);
                for(int i=0; i<split.length; i++) {
                    if(i>0) {
                        String showName = "";
                        String showId = "";

                        String htmlPart = split[i];
                        htmlPart = htmlPart.replace("href=\"views.php?type=epsbyshow&showid=", "");
                        //Get the showid
                        String showSeperator = "\">";
                        int showIdSeperatorIndex = StringUtils.indexOf(htmlPart, showSeperator);
                        showId = htmlPart.substring(0, showIdSeperatorIndex);
                        htmlPart = htmlPart.replace(showId + showSeperator, "");
                        //Get the showName
                        showName = htmlPart.substring(0, StringUtils.indexOf(htmlPart, "</a></td>"));

                        shows.add(new Show(showName, showId));
                    }
                }
            }
        }
        return shows;
    }

    //TODO uncomment and test further!!!
//    public void getFavoriteShows(User user) throws UnsupportedHttpPostEncodingException, InternetConnectivityException, LoginFailedException {
//        HttpClient httpClient = new DefaultHttpClient();
//        String username = user.getUsername();
//        login(httpClient, username, user.getPassword());
//
//        HttpGet get = new HttpGet(MYEPISODES_FAVO_IGNORE_PAGE);
////        HttpParams params = .getDefaultParams();
////        get.setParams();
//
//		String responsePage = "";
//        HttpResponse response;
//        try {
//            response = httpClient.execute(get);
//            responsePage = EntityUtils.toString(response.getEntity());
//        } catch (ClientProtocolException e) {
//            String message = "Could not connect to host.";
//			Log.e(LOG_TAG, message, e);
//			throw new InternetConnectivityException(message, e);
//        } catch (UnknownHostException e) {
//			String message = "Could not connect to host.";
//			Log.e(LOG_TAG, message, e);
//			throw new InternetConnectivityException(message, e);
//		} catch (IOException e) {
//            String message = "Search on MyEpisodes failed.";
//            Log.w(LOG_TAG, message, e);
//            throw new LoginFailedException(message, e);
//        }
//    }
}
