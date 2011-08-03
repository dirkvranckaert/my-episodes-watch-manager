package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.domain.FeedItem;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.exception.*;
import eu.vranckaert.episodeWatcher.utils.DateUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.pojava.datetime.DateTime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EpisodesService {
	private static final String LOG_TAG = EpisodesService.class.getSimpleName();

    private UserService userService;

    public EpisodesService() {
        userService = new UserService();
    }

    public List<Episode> retrieveEpisodes(EpisodeType episodesType,final User user) throws InternetConnectivityException, Exception {
        String encryptedPassword = userService.encryptPassword(user.getPassword());
        URL feedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
        
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
	            String[] episodeInfo = title.toString().split(MyEpisodeConstants.FEED_TITLE_SEPERATOR);
	            if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS) {
	                episode.setShowName(episodeInfo[0].trim());
	                getSeasonAndEpisodeNumber(episodeInfo[1], episode);
	                episode.setName(episodeInfo[2].trim());
                    String airDateString = episodeInfo[3].trim();
                    Date airDate = null;
                    try {
                        airDate = parseDate(airDateString);
                    } catch (Exception e) {
                        airDate = DateUtil.convertToDate(airDateString);
                    }
	                episode.setAirDate(airDate);
	                episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
	                episode.setId();
	                
	                Log.d(LOG_TAG, "Episode from feed: " + episode.getShowName() + " - S" + episode.getSeasonString() + "E" + episode.getEpisodeString());
	                
	                episodes.add(episode);
	            } else if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS - 1) {
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
        List<Episode> episodes = new ArrayList<Episode>();
        episodes.add(episode);
        watchedEpisodes(episodes, user);
    }

    public void watchedEpisodes(List<Episode> episodes, User user) throws LoginFailedException
            , ShowUpdateFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
        HttpClient httpClient = new DefaultHttpClient();

        userService.login(httpClient, user.getUsername(), user.getPassword());

        for(Episode episode : episodes) {
            markAnEpisode(0, httpClient, episode);
        }

        httpClient.getConnectionManager().shutdown();
    }
    
    public void acquireEpisode(Episode episode, User user) throws LoginFailedException
		    , ShowUpdateFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
		List<Episode> episodes = new ArrayList<Episode>();
        episodes.add(episode);
        acquireEpisodes(episodes, user);
	}

    public void acquireEpisodes(List<Episode> episodes, User user) throws LoginFailedException
		    , ShowUpdateFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
		HttpClient httpClient = new DefaultHttpClient();

		userService.login(httpClient, user.getUsername(), user.getPassword());
        for(Episode episode : episodes) {
		    markAnEpisode(1, httpClient, episode);
        }

		httpClient.getConnectionManager().shutdown();
	}
    
    private void markAnEpisode(int EpisodeStatus, HttpClient httpClient, Episode episode) throws ShowUpdateFailedException, InternetConnectivityException {
    	String urlRep = "";
    	
    	urlRep = EpisodeStatus == 0 ? MyEpisodeConstants.MYEPISODES_UPDATE_WATCH : MyEpisodeConstants.MYEPISODES_UPDATE_ACQUIRE;

        urlRep = urlRep.replace(MyEpisodeConstants.MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT, String.valueOf(episode.getEpisode()));
        urlRep = urlRep.replace(MyEpisodeConstants.MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT, String.valueOf(episode.getSeason()));
        urlRep = urlRep.replace(MyEpisodeConstants.MYEPISODES_UPDATE_PAGE_SHOWID_REPLACEMENT, episode.getMyEpisodeID());

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
	        String[] episodeInfoNumber = seasonEpisodeNumber.split(MyEpisodeConstants.SEASON_EPISODE_NUMBER_SEPERATOR);
	        episode.setSeason(Integer.parseInt(episodeInfoNumber[0].trim()));
	        episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));
    	}
    }
    
    private URL buildEpisodesUrl(EpisodeType episodesType,final String username, final String encryptedPassword) throws FeedUrlBuildingFaildException {
    	String urlRep = "";
    	switch(episodesType)
        {
	        case EPISODES_TO_WATCH: urlRep = MyEpisodeConstants.UNWATCHED_EPISODES_URL;
	        break;
	        case EPISODES_TO_ACQUIRE: urlRep = MyEpisodeConstants.UNAQUIRED_EPISODES_URL;
	        break;
	        case EPISODES_TO_YESTERDAY: urlRep = MyEpisodeConstants.YESTERDAY_EPISODES_URL;
	        break;
	        case EPISODES_COMING: urlRep = MyEpisodeConstants.COMING_EPISODES_URL;
	        break; 
        }
    	
        urlRep = urlRep.replace(MyEpisodeConstants.UID_REPLACEMENT_STRING, username);
        urlRep = urlRep.replace(MyEpisodeConstants.PWD_REPLACEMENT_STRING, encryptedPassword);

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
}
