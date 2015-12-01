package eu.vranckaert.episodeWatcher.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import eu.vranckaert.episodeWatcher.BuildConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.pojava.datetime.DateTime;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.controllers.EpisodesController;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.domain.FeedItem;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;
import eu.vranckaert.episodeWatcher.exception.FeedUrlBuildingFaildException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowUpdateFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.utils.DateUtil;

public class EpisodesService {
    private static final String LOG_TAG = EpisodesService.class.getSimpleName();
    private UserService userService;

    public EpisodesService() {
        userService = new UserService();
    }

    public HttpClient getHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        return httpClient;
    }

    public List<Episode> retrieveUnlimitedNumberOfEpisodes(final EpisodeType episodesType, final User user) throws Exception {
        List<Episode> cachedEpisodes = CacheService.getEpisodes(episodesType);
        if (cachedEpisodes != null) {
            return cachedEpisodes;
        }

        if (true && BuildConfig.DEBUG) {
            SystemClock.sleep(5000L);

            String[] shows = new String[]{"Fargo", "How I Met Your Mother", "The Big Bang Theory", "Dexter", "Thuis", "Familie", "24", "Chuck", "The Americans", "The White House"};

            List<Episode> episodes = new ArrayList<>();
            int numberOfEpisodes = (int) (Math.random() * 10);
            for (int i=0; i<numberOfEpisodes; i++) {
                String showName = shows[i];
                String episodeName = "Name of the episode " + i;

                Episode episode = new Episode();
                episode.setName(episodeName);
                episode.setShowName(showName);
                episode.setMyEpisodeID("23525" + i);
                episode.setType(episodesType);
                episode.setSeason(i);
                episode.setEpisode(i);
                episode.setAirDate(new Date());
                episodes.add(episode);
            }

            Episode fixedEpisode = new Episode();
            fixedEpisode.setAirDate(new Date());
            fixedEpisode.setShowName(shows[0]);
            fixedEpisode.setEpisode(5);
            fixedEpisode.setSeason(3);
            fixedEpisode.setName("Some random episode");
            fixedEpisode.setType(episodesType);
            episodes.add(fixedEpisode);

            //CacheService.storeEpisodes(episodes, episodesType);

            return episodes;
        }

        String encryptedPassword = userService.encryptPassword(user.getPassword());

        URL normalFeedUrl;
        URL fullFeedUrl;

        if (EpisodeType.EPISODES_TO_WATCH.equals(episodesType)) {
            normalFeedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
            HttpClient httpClientAllEps = getHttpClient();
            MyEpisodeConstants.EXTENDED_EPISODES_XML = downloadFullUnwatched(httpClientAllEps, user, true).toString();
            fullFeedUrl = new URL("http://127.0.0.1");
        } else if (EpisodeType.EPISODES_TO_ACQUIRE.equals(episodesType)) {
            normalFeedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
            HttpClient httpClientAllEps = getHttpClient();
            MyEpisodeConstants.EXTENDED_EPISODES_XML = downloadFullUnwatched(httpClientAllEps, user, false).toString();
            fullFeedUrl = new URL("http://127.0.0.1");
        } else {
            normalFeedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
            fullFeedUrl = null;
        }

        List<Episode> episodes = getEpisodesForUrl(episodesType, normalFeedUrl);
        if (episodes.size() == 200) {
            List<Episode> fullEpisodes = getEpisodesForUrl(episodesType, fullFeedUrl);

            for (Episode episode : fullEpisodes) {
                if (!episodes.contains(episode)) {
                    episodes.add(episode);
                }
            }
        }

        CacheService.storeEpisodes(episodes, episodesType);
        return episodes;
    }

    @NonNull
    private List<Episode> getEpisodesForUrl(EpisodeType episodesType, URL normalFeedUrl) throws Exception {
        RssFeedParser rssFeedParser = new SaxRssFeedParser();
        Feed rssFeed;
        rssFeed = rssFeedParser.parseFeed(normalFeedUrl);

        List<Episode> episodes = new ArrayList<>(0);

        for (FeedItem item : rssFeed.getItems()) {
            Episode episode = new Episode();

            StringBuilder title = new StringBuilder(item.getTitle());

            if (title.length() > 0) {
                //Sample title: [ Reaper ][ 01x14 ][ Rebellion ][ 23-Apr-2008 ]
                title = title.replace(0, 2, ""); //Strip off first bracket [
                title = title.replace(title.length() - 2, title.length(), ""); //Strip off last bracket ]
                String[] episodeInfo = title.toString().split(MyEpisodeConstants.FEED_TITLE_SEPERATOR);
                episode.setShowName(episodeInfo[0].trim());
                getSeasonAndEpisodeNumber(episodeInfo[1], episode);
                Date airDate = null;
                if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS) {
                    episode.setName(episodeInfo[2].trim());
                    String airDateString = episodeInfo[3].trim();
                    episode.setType(episodesType);

                    airDate = DateUtil.convertToDate(airDateString);

                    episode.setAirDate(airDate);
                    episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
                    episode.setTVRageWebSite(item.getLink());

                    Log.d(LOG_TAG,
                            "Episode from feed: " + episode.getShowName() + " - S" + episode.getSeasonString() + "E" +
                                    episode.getEpisodeString());
                } else if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS - 1) {
                    //Solves problem mentioned in Issue 20
                    episode.setName(episodeInfo[2].trim() + "...");
                    episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
                    episode.setTVRageWebSite(item.getLink());
                } else {
                    String message = "Problem parsing a feed item. Feed details: " + item.toString();
                    Log.e(LOG_TAG, message);
                }


                if (!episodesType.equals(EpisodeType.EPISODES_COMING)) {
                    episodes.add(episode);
                } else {
                    Calendar rightNow = Calendar.getInstance();
                    rightNow.add(Calendar.DATE, -1);
                    Date yesterday = rightNow.getTime();
                    if (airDate != null) {
                        if (airDate.after(yesterday)) {
                            episodes.add(episode);
                        }
                    }
                }
            }
        }

        return episodes;
    }

    public List<Episode> retrieveEpisodes(EpisodeType episodesType, final User user)
            throws InternetConnectivityException, Exception {
        String encryptedPassword = userService.encryptPassword(user.getPassword());

        URL feedUrl;

        //downloadFullUnwatched list 
        if (episodesType.toString() == "EPISODES_TO_WATCH") {
            //override with download from http://myepisodes.com/views.php

            Log.d(LOG_TAG, "MyEpisodeConstants.DAYS_BACK_ENABLED: " + MyEpisodeConstants.DAYS_BACK_ENABLED);
            Log.d(LOG_TAG, "MyEpisodeConstants.DAYS_BACK_CP: " + MyEpisodeConstants.DAYS_BACK_CP);


            if (MyEpisodeConstants.DAYS_BACK_ENABLED) {
                HttpClient httpClientAllEps = getHttpClient();

                //this is causing the issues.
                MyEpisodeConstants.EXTENDED_EPISODES_XML =
                        downloadFullUnwatched(httpClientAllEps, user, true).toString();

                feedUrl = new URL("http://127.0.0.1"); //this is used in the parse to confirm that this has been run.
            } else {
                //if not enabling the extended functions
                feedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
            }
        } else if (episodesType.toString() == "EPISODES_TO_ACQUIRE") {
            Log.d(LOG_TAG, "MyEpisodeConstants.DAYS_BACK_ENABLED: " + MyEpisodeConstants.DAYS_BACK_ENABLED);
            Log.d(LOG_TAG, "MyEpisodeConstants.DAYS_BACK_CP: " + MyEpisodeConstants.DAYS_BACK_CP);


            if (MyEpisodeConstants.DAYS_BACK_ENABLED) {
                HttpClient httpClientAllEps = getHttpClient();

                //this is causing the issues.
                MyEpisodeConstants.EXTENDED_EPISODES_XML =
                        downloadFullUnwatched(httpClientAllEps, user, false).toString();

                feedUrl = new URL("http://127.0.0.1"); //this is used in the parse to confirm that this has been run.
            } else {
                //if not enabling the extended functions
                feedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
            }
        } else {
            feedUrl = buildEpisodesUrl(episodesType, user.getUsername().replace(" ", "%20"), encryptedPassword);
        }

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
                title = title.replace(title.length() - 2, title.length(), ""); //Strip off last bracket ]
                String[] episodeInfo = title.toString().split(MyEpisodeConstants.FEED_TITLE_SEPERATOR);
                episode.setShowName(episodeInfo[0].trim());
                getSeasonAndEpisodeNumber(episodeInfo[1], episode);
                Date airDate = null;
                if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS) {
                    episode.setName(episodeInfo[2].trim());
                    String airDateString = episodeInfo[3].trim();
                    episode.setType(episodesType);

                    airDate = DateUtil.convertToDate(airDateString);

                    episode.setAirDate(airDate);
                    episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
                    episode.setTVRageWebSite(item.getLink());

                    Log.d(LOG_TAG,
                            "Episode from feed: " + episode.getShowName() + " - S" + episode.getSeasonString() + "E" +
                                    episode.getEpisodeString());
                } else if (episodeInfo.length == MyEpisodeConstants.FEED_TITLE_EPISODE_FIELDS - 1) {
                    //Solves problem mentioned in Issue 20
                    episode.setName(episodeInfo[2].trim() + "...");
                    episode.setMyEpisodeID(item.getGuid().split("-")[0].trim());
                    episode.setTVRageWebSite(item.getLink());
                } else {
                    String message = "Problem parsing a feed item. Feed details: " + item.toString();
                    Log.e(LOG_TAG, message);
                }


                if (!episodesType.equals(EpisodeType.EPISODES_COMING)) {
                    episodes.add(episode);
                } else {
                    Calendar rightNow = Calendar.getInstance();
                    rightNow.add(Calendar.DATE, -1);
                    Date yesterday = rightNow.getTime();
                    if (airDate != null) {
                        if (airDate.after(yesterday)) {
                            episodes.add(episode);
                        }
                    }
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
        HttpClient httpClient = getHttpClient();

        userService.login(httpClient, user.getUsername(), user.getPassword());

        for (Episode episode : episodes) {
            markAnEpisode(0, httpClient, episode);
            EpisodesController.getInstance().deleteEpisode(EpisodeType.EPISODES_COMING, episode);
            EpisodesController.getInstance().deleteEpisode(EpisodeType.EPISODES_TO_ACQUIRE, episode);
            EpisodesController.getInstance().deleteEpisode(EpisodeType.EPISODES_TO_WATCH, episode);
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
        HttpClient httpClient = getHttpClient();

        userService.login(httpClient, user.getUsername(), user.getPassword());

        for (Episode episode : episodes) {
            markAnEpisode(1, httpClient, episode);
            EpisodesController.getInstance().deleteEpisode(EpisodeType.EPISODES_TO_ACQUIRE, episode);
            EpisodesController.getInstance().addEpisode(EpisodeType.EPISODES_TO_WATCH, episode);
        }

        httpClient.getConnectionManager().shutdown();
    }

    private void markAnEpisode(int EpisodeStatus, HttpClient httpClient, Episode episode)
            throws ShowUpdateFailedException, InternetConnectivityException {
        String urlRep = "";

        urlRep = EpisodeStatus == 0 ? MyEpisodeConstants.MYEPISODES_UPDATE_WATCH :
                MyEpisodeConstants.MYEPISODES_UPDATE_ACQUIRE;

        urlRep = urlRep.replace(MyEpisodeConstants.MYEPISODES_UPDATE_PAGE_EPISODE_REPLACEMENT,
                String.valueOf(episode.getEpisode()));
        urlRep = urlRep.replace(MyEpisodeConstants.MYEPISODES_UPDATE_PAGE_SEASON_REPLACEMENT,
                String.valueOf(episode.getSeason()));
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
    
    
    /*
     * parse the views.php page to show a full list of unwatched apps
     */

    private StringWriter downloadFullUnwatched(HttpClient httpClient, User user, boolean isWatched)
            throws LoginFailedException, ShowUpdateFailedException, InternetConnectivityException,
            UnsupportedHttpPostEncodingException {
        String urlRep = MyEpisodeConstants.MYEPISODES_FULL_UNWATCHED_LISTING;
        urlRep = "https://www.myepisodes.com/ajax/service.php?mode=view_privatelist";
        //login to myepisodes
        userService.login(httpClient, user.getUsername(), user.getPassword());

        int status = 200;

        StringWriter sw = new StringWriter();

        try {
            // Get current days back so users view is not broken.
            String[] controlPanelSettings = getDaysBack(httpClient);

            // Set the days back to retrieve unwatched eps.
            setDaysBack(controlPanelSettings, httpClient, false);

            // set the filter to only show eps that have not yet been watched
            setViewFilters(true, isWatched, httpClient);

            Log.d(LOG_TAG, "DOWNLOADING FULL LIST");
            //get request to download the myviews.php for processing to xml
            //HttpGet get = new HttpGet(urlRep);
            HttpPost post = new HttpPost(urlRep);
            //start the process of downloading the files.
            //HttpResponse response = httpClient.execute(get);
            HttpResponse response = httpClient.execute(post);
            status = response.getStatusLine().getStatusCode();

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();

            // If the response does not enclose an entity, there is no need to worry about connection release
            if (entity != null) {
                InputStream instream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

                StringBuilder HTML = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    HTML.append(line);
                }

                String HTMLtoDecode = HTML.toString();

                //read html file.
                int startTable = HTMLtoDecode.indexOf(
                        "<table class=\"mylist\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">") +
                        274;
                HTMLtoDecode = HTMLtoDecode.substring(startTable);

                int endTable = HTMLtoDecode.indexOf("</table>");
                int rest = HTMLtoDecode.length() - endTable;
                endTable -= (8 + rest);

                if (endTable < 1) {
                    //prevent index out of bounds exception below when defining HTMLtoDecode.
                    endTable = 1;
                    Log.d(LOG_TAG, "No episodes to display");
                }

                HTMLtoDecode = HTMLtoDecode.substring(1, endTable);

                //split each table row into an array for processing
                String[] EpisodeTable = HTMLtoDecode.split("</tr>");

                Log.d(LOG_TAG, "Number of eps found: " + EpisodeTable.length);

                //Download complete, now start rebuilding the RSS feed file.
                XmlSerializer xs = Xml.newSerializer();

                xs.setOutput(sw);
                xs.startDocument(null, null);
                xs.startTag(null, "channel");

                for (String a : EpisodeTable) {
                    try {
                        //split each column into a array
                        if (a.equals("")) {
                            Log.d(LOG_TAG, "No Episodes found.");
                        } else {
                            String[] rowProcess = a.split("</td>");

                            //name of show
                            int indexName = rowProcess[2].indexOf("showname\">") + 10;
                            String showPart = rowProcess[2].substring(indexName);
                            indexName = showPart.indexOf(">") + 1;
                            int indexNameEndTag = showPart.indexOf("</a>");
                            String show = showPart.substring(indexName, indexNameEndTag);

                            // get Series and Episode
                            String seriesEps = rowProcess[3].substring(28);

                            //Get episode name
                            int indexEp = rowProcess[4].indexOf("epname\">") + 8;
                            String episodeName = rowProcess[4].substring(indexEp);

                            //Get episode link - doesn't work yet.
                            int indexEpLink = rowProcess[4].indexOf("a href=") + 8;
                            String episodeLink = rowProcess[4].substring(indexEpLink);
                            int indexEpLink1 = episodeLink.indexOf("\"");
                            episodeLink = episodeLink.substring(0, indexEpLink1);
                            episodeLink = "";

                            //get air date
                            int indexAirDate = rowProcess[0].length() - 15;
                            String airDate = rowProcess[0].substring(indexAirDate, indexAirDate + 11);

                            boolean watchedEpisode = rowProcess[6].contains("checked");
                            boolean acquiredEpisode = rowProcess[5].contains("checked");

                            boolean keepEpisode = false;
                            if (!watchedEpisode || !acquiredEpisode) {
                                if (isWatched && !watchedEpisode && acquiredEpisode) {
                                    keepEpisode = true;
                                } else if (!isWatched && !acquiredEpisode) {
                                    keepEpisode = true;
                                }
                            }

                            if (keepEpisode) {
                                //Get guid
                                int indexGUID = rowProcess[5].indexOf("name=") + 7;
                                String guid = rowProcess[5].substring(indexGUID);
                                int indexGUID1 = guid.indexOf("\"");
                                guid = guid.substring(0, indexGUID1);

                                String headerRow =
                                        "[ " + show + " ]" + "[ " + seriesEps + " ]" + "[ " + episodeName + " ]" +
                                                "[ " +
                                                airDate + " ]";

                                xs.startTag(null, "item");
                                xs.startTag(null, "guid");
                                xs.text(guid);
                                xs.endTag(null, "guid");

                                xs.startTag(null, "title");
                                xs.text(headerRow);
                                xs.endTag(null, "title");

                                xs.startTag(null, "link");
                                xs.text(episodeLink);
                                xs.endTag(null, "link");

                                xs.startTag(null, "description");
                                xs.endTag(null, "description");

                                xs.endTag(null, "item");
                            }
                        }
                    } catch (Exception e) {
                        // NA
                    }
                }

                xs.endTag(null, "channel");
                xs.endDocument();
                Log.d(LOG_TAG, "Finished Download and RSS built");
                Log.d(LOG_TAG, "Resetting webview controlpanel settings and views.php filters");
                //set the days back to what they are in the settigns
                setDaysBack(controlPanelSettings, httpClient, true);
                setViewFilters(false, false, httpClient);
            }
        } catch (UnknownHostException e) {
            String message = "Could not connect to host.";
            Log.e(LOG_TAG, message, e);
            throw new InternetConnectivityException(message, e);
        } catch (IOException e) {
            String message = "Error downloading and processing " + urlRep;
            Log.w(LOG_TAG, message, e);
            throw new ShowUpdateFailedException(message, e);
        }

        if (status != 200) {
            String message =
                    "Error downloading and processing, failed with status code " + status + " for URL " + urlRep;
            Log.w(LOG_TAG, message);
            throw new ShowUpdateFailedException(message);
        } else {
            Log.i(LOG_TAG, "Successfully downloaded full episode list from url " + urlRep + " (" + ")");
        }
        return sw;
    }

    //get the users web browser settings to keep them the same
    private String[] getDaysBack(HttpClient httpClient) {
        String[] controlPanelSettings = new String[20];

        //control panel settings to read.
        String eps_timezone;
        String eps_time_offset;
        String dateformat;
        String timeformat;
        String eps_number_format;
        String ce_dback;
        String ce_dforward;
        String colorpast1;
        String colorpast2;
        String colortoday;
        String color1;
        String color2;
        String colorhover;
        String sw_acquire_delay;
        String cal_firstday;
        String action;
        String loginpage;
        String sw_hidefuture;
        String sw_presentonly;
        String sw_currentseasononly;

        //default values should be same for all users.
        action = "Save";

        try {

            HttpGet get = new HttpGet(MyEpisodeConstants.MYEPISODES_CONTROL_PANEL);
            //start the process of downloading the files.
            HttpResponse response = httpClient.execute(get);
            response.getStatusLine().getStatusCode();

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();

            // If the response does not enclose an entity, there is no need to worry about connection release
            if (entity != null) {
                InputStream instream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

                StringBuilder HTMLcp = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    HTMLcp.append(line);
                }

                //need to store and send all the settings sending just the daysback setting doesn't work.
                String settingsHTML = HTMLcp.toString();

                ce_dback = settingsHTML.substring(settingsHTML.indexOf("name=\"ce_dback\" value=\"") + 23);
                ce_dback = ce_dback.substring(0, ce_dback.indexOf("\""));

                eps_time_offset =
                        settingsHTML.substring(settingsHTML.indexOf("name=\"eps_time_offset\" value=\"") + 30);
                eps_time_offset = eps_time_offset.substring(0, eps_time_offset.indexOf("\""));

                dateformat = settingsHTML.substring(settingsHTML.indexOf("name=\"dateformat\" value=\"") + 25);
                dateformat = dateformat.substring(0, dateformat.indexOf("\""));

                timeformat = settingsHTML.substring(settingsHTML.indexOf("name=\"timeformat\" value=\"") + 25);
                timeformat = timeformat.substring(0, timeformat.indexOf("\""));

                eps_number_format =
                        settingsHTML.substring(settingsHTML.indexOf("name=\"eps_number_format\" value=\"") + 32);
                eps_number_format = eps_number_format.substring(0, eps_number_format.indexOf("\""));

                ce_dforward = settingsHTML.substring(settingsHTML.indexOf("name=\"ce_dforward\" value=\"") + 26);
                ce_dforward = ce_dforward.substring(0, ce_dforward.indexOf("\""));

                colorpast1 = settingsHTML.substring(settingsHTML.indexOf("'link1');\" type=\"text\" value=\"") + 30);
                colorpast1 = colorpast1.substring(0, colorpast1.indexOf("\""));

                colorpast2 = settingsHTML.substring(settingsHTML.indexOf("'link2');\" type=\"text\" value=\"") + 30);
                colorpast2 = colorpast2.substring(0, colorpast2.indexOf("\""));

                colortoday = settingsHTML.substring(settingsHTML.indexOf("'link3');\" type=\"text\" value=\"") + 30);
                colortoday = colortoday.substring(0, colortoday.indexOf("\""));

                color1 = settingsHTML.substring(settingsHTML.indexOf("'link4');\" type=\"text\" value=\"") + 30);
                color1 = color1.substring(0, color1.indexOf("\""));

                color2 = settingsHTML.substring(settingsHTML.indexOf("'link5');\" type=\"text\" value=\"") + 30);
                color2 = color2.substring(0, color2.indexOf("\""));

                colorhover = settingsHTML.substring(settingsHTML.indexOf("'link6');\" type=\"text\" value=\"") + 30);
                colorhover = colorhover.substring(0, colorhover.indexOf("\""));

                sw_acquire_delay =
                        settingsHTML.substring(settingsHTML.indexOf("name=\"sw_acquire_delay\" value=\"") + 31);
                sw_acquire_delay = sw_acquire_delay.substring(0, sw_acquire_delay.indexOf("\""));

                cal_firstday = settingsHTML.substring(settingsHTML.indexOf("name=\"cal_firstday\""));
                cal_firstday = cal_firstday
                        .substring(cal_firstday.indexOf("selected") - 3, cal_firstday.indexOf("selected") - 2);

                int timeZoneIndex = settingsHTML.indexOf("name=\"eps_timezone\"");
                eps_timezone = settingsHTML.substring(timeZoneIndex);
                int timeZoneSelectedIndex = eps_timezone.indexOf("</select>");
                String TimezoneRange = settingsHTML.substring(timeZoneIndex, timeZoneIndex + timeZoneSelectedIndex);
                String[] SplitTimeZones = TimezoneRange.split("</option>");

                for (String a : SplitTimeZones) {
                    int selectedIndex = a.indexOf("selected");
                    if (selectedIndex > 1) {
                        eps_timezone = a.substring(a.indexOf(">") + 1);
                    }
                }

                int loginpageIndex = settingsHTML.indexOf("name=\"loginpage\"") + 17;
                loginpage = settingsHTML.substring(loginpageIndex);
                int loginpageSelectedIndex = loginpage.indexOf("</select>");
                String loginpageRange = settingsHTML.substring(loginpageIndex, loginpageIndex + loginpageSelectedIndex);
                String[] Splitloginpage = loginpageRange.split("</option>");

                for (String a : Splitloginpage) {
                    int selectedIndex = a.indexOf("selected");
                    if (selectedIndex > 1) {
                        loginpage = a.substring(a.indexOf("=") + 2);
                        loginpage = loginpage.substring(0, loginpage.indexOf("\""));
                    }
                }

                sw_hidefuture = settingsHTML.substring(settingsHTML.indexOf("name=\"sw_hidefuture\""));
                sw_hidefuture = sw_hidefuture.substring(21, 28);
                if (sw_hidefuture.equals("checked")) {
                    sw_hidefuture = "on";
                } else {
                    sw_hidefuture = null;
                }

                sw_presentonly = settingsHTML.substring(settingsHTML.indexOf("name=\"sw_presentonly\""));
                sw_presentonly = sw_presentonly.substring(22, 29);
                if (sw_presentonly.equals("checked")) {
                    sw_presentonly = "on";
                } else {
                    sw_presentonly = null;
                }

                sw_currentseasononly = settingsHTML.substring(settingsHTML.indexOf("name=\"sw_currentseasononly\""));
                sw_currentseasononly = sw_currentseasononly.substring(28, 35);
                if (sw_currentseasononly.equals("checked")) {
                    sw_currentseasononly = "on";
                } else {
                    sw_currentseasononly = null;
                }

                controlPanelSettings[0] = eps_timezone;
                controlPanelSettings[1] = eps_time_offset;
                controlPanelSettings[2] = dateformat;
                controlPanelSettings[3] = timeformat;
                controlPanelSettings[4] = eps_number_format;
                controlPanelSettings[5] = ce_dback;
                controlPanelSettings[6] = ce_dforward;
                controlPanelSettings[7] = colorpast1;
                controlPanelSettings[8] = colorpast2;
                controlPanelSettings[9] = colortoday;
                controlPanelSettings[10] = color1;
                controlPanelSettings[11] = color2;
                controlPanelSettings[12] = colorhover;
                controlPanelSettings[13] = sw_acquire_delay;
                controlPanelSettings[14] = cal_firstday;
                controlPanelSettings[15] = action;
                controlPanelSettings[16] = loginpage;
                controlPanelSettings[17] = sw_hidefuture;
                controlPanelSettings[18] = sw_presentonly;
                controlPanelSettings[19] = sw_currentseasononly;

                StringBuilder builder = new StringBuilder();
                for (String value : controlPanelSettings) {
                    builder.append("   " + value);
                }
                //display all settings which will be set for cp.php
                //Log.d(LOG_TAG, builder.toString());
            }
        } catch (IOException e) {
            String message = "Error setting days back";
            Log.e(LOG_TAG, message, e);
        }
        return controlPanelSettings;
    }

    private void setDaysBack(String[] controlPanelSettings, HttpClient httpClient, Boolean restore) {
        //send POST to set just the number of days in the past to show..
        Log.d(LOG_TAG, "Setting number of days back");
        String[] controlPanelOrder = new String[20];
        controlPanelOrder[0] = "eps_timezone";
        controlPanelOrder[1] = "eps_time_offset";
        controlPanelOrder[2] = "dateformat";
        controlPanelOrder[3] = "timeformat";
        controlPanelOrder[4] = "eps_number_format";
        controlPanelOrder[5] = "ce_dback";
        controlPanelOrder[6] = "ce_dforward";
        controlPanelOrder[7] = "colorpast1";
        controlPanelOrder[8] = "colorpast2";
        controlPanelOrder[9] = "colortoday";
        controlPanelOrder[10] = "color1";
        controlPanelOrder[11] = "color2";
        controlPanelOrder[12] = "colorhover";
        controlPanelOrder[13] = "sw_acquire_delay";
        controlPanelOrder[14] = "cal_firstday";
        controlPanelOrder[15] = "action";
        controlPanelOrder[16] = "loginpage";

        //if not commented sets the value in settings need to increase the array size above as well
        controlPanelOrder[17] = "sw_hidefuture";
        controlPanelOrder[18] = "sw_presentonly";
        controlPanelOrder[19] = "sw_currentseasononly";


        try {
            HttpPost httppostCP = new HttpPost(MyEpisodeConstants.MYEPISODES_CONTROL_PANEL);
            List<NameValuePair> nameValuePairsCP = new ArrayList<NameValuePair>(17);

            for (int i = 0; i < controlPanelOrder.length; i++) {
                //when setting the value for the download
                if (i == 5 && restore == false && controlPanelSettings[i] != null) {
                    nameValuePairsCP.add(new BasicNameValuePair(controlPanelOrder[i], MyEpisodeConstants.DAYS_BACK_CP));
                } else if (i == 6 && restore == false && controlPanelSettings[i] != null) {
                    nameValuePairsCP.add(new BasicNameValuePair(controlPanelOrder[i], "1"));
                } else {
                    if (controlPanelSettings[i] != null) {
                        nameValuePairsCP.add(new BasicNameValuePair(controlPanelOrder[i], controlPanelSettings[i]));
                    }
                }
            }
            httppostCP.setEntity(new UrlEncodedFormEntity(nameValuePairsCP));

            // 	Execute HTTP Post Request
            HttpResponse responsePostCP = httpClient.execute(httppostCP);
            responsePostCP.getStatusLine();

            EntityUtils.toString(responsePostCP.getEntity());

        } catch (IOException e) {
            String message = "Error setting days back";
            Log.e(LOG_TAG, message, e);
        }
    }

    private void setViewFilters(Boolean setForDownload, Boolean isWatched, HttpClient httpClient) {
        HttpPost httppost = new HttpPost(MyEpisodeConstants.MYEPISODES_FULL_UNWATCHED_LISTING);
        try {
            if (setForDownload) {
                //send POST request to only show episodes with the filter Watch
                //eps_filters[]=2&action=Filter
                if (isWatched) {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("eps_filters[]", "2"));
                    nameValuePairs.add(new BasicNameValuePair("action", "Filter"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                } else {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("eps_filters[]", "1"));
                    nameValuePairs.add(new BasicNameValuePair("action", "Filter"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                }
            } else {
                //set back to user default settings for the view filters
                // need to work on detecting the settings to use.
                List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>(2);
                nameValuePairs2.add(new BasicNameValuePair("eps_filters[]", "1"));
                nameValuePairs2.add(new BasicNameValuePair("eps_filters[]", "2"));
                nameValuePairs2.add(new BasicNameValuePair("action", "Filter"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs2));
            }
            // 	Execute HTTP Post Request
            HttpResponse responsePost = httpClient.execute(httppost);
            responsePost.getStatusLine();

            EntityUtils.toString(responsePost.getEntity());
            // Get hold of the response entity - need to read to the end to prevent "Invalid use of SingleClient...."
            /*HttpEntity entity = responsePost.getEntity();
            if (entity != null) {
    			InputStream instream = entity.getContent();    			
    			BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
    			StringBuilder HTMLcp = new StringBuilder();
    			String line;
    			while ((line = reader.readLine()) != null) {
    				HTMLcp.append(line);
    			}
    		}*/

        } catch (IOException e) {
            String message = "Error setting days back";
            Log.e(LOG_TAG, message, e);
        }


    }

    private Date parseDate(String date) {
        DateTime parsedDate = new DateTime(date);
        return parsedDate.toDate();
    }

    private void getSeasonAndEpisodeNumber(String seasonEpisodeNumber, Episode episode) {
        if (seasonEpisodeNumber.startsWith("S")) {
            String[] episodeInfoNumber = seasonEpisodeNumber.split("E");
            episode.setSeason(Integer.parseInt(episodeInfoNumber[0].replace("S", "").trim()));
            episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));
        } else {
            String[] episodeInfoNumber = seasonEpisodeNumber.split(MyEpisodeConstants.SEASON_EPISODE_NUMBER_SEPERATOR);
            episode.setSeason(Integer.parseInt(episodeInfoNumber[0].trim()));
            episode.setEpisode(Integer.parseInt(episodeInfoNumber[1].trim()));
        }
    }

    private URL buildEpisodesUrl(EpisodeType episodesType, final String username, final String encryptedPassword)
            throws FeedUrlBuildingFaildException {
        String urlRep = "";
        switch (episodesType) {
            case EPISODES_TO_WATCH:
                urlRep = MyEpisodeConstants.UNWATCHED_EPISODES_URL;
                break;
            case EPISODES_TO_ACQUIRE:
                urlRep = MyEpisodeConstants.UNAQUIRED_EPISODES_URL;
                break;
            case EPISODES_TO_YESTERDAY1:
                urlRep = MyEpisodeConstants.YESTERDAY_EPISODES_URL;
                break;
            case EPISODES_TO_YESTERDAY2:
                urlRep = MyEpisodeConstants.YESTERDAY2_EPISODES_URL;
                break;
            case EPISODES_COMING:
                urlRep = MyEpisodeConstants.COMING_EPISODES_URL;
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
