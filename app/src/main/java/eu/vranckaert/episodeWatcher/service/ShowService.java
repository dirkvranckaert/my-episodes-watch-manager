package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.ShowAction;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.ShowAddFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.utils.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ShowService {
    private static final String LOG_TAG = ShowService.class.getSimpleName();

    private UserService userService;

    public ShowService() {
        userService = new UserService();
    }

    public List<Show> searchShows(String search, User user) throws UnsupportedHttpPostEncodingException, InternetConnectivityException, LoginFailedException {
        OkHttpClient httpClient = EpisodesService.getOkHttpClient();
        String username = user.getUsername();
        userService.login(httpClient, username, user.getPassword());

    	List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_SEARCH_PAGE_PARAM_SHOW, search));
        nvps.add(new BasicNameValuePair(MyEpisodeConstants.MYEPISODES_FORM_PARAM_ACTION, MyEpisodeConstants.MYEPISODES_SEARCH_PAGE_PARAM_ACTION_VALUE));

        Request request = EpisodesService.buildPostRequest(httpClient, MyEpisodeConstants.MYEPISODES_SEARCH_PAGE, nvps);

		String responsePage = "";
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            responsePage = response.body().string();
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
        String[] split = html.split(MyEpisodeConstants.MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_SEARCH_RESULTS);
        if(split.length == 2) {
            split = split[1].split(MyEpisodeConstants.MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TABLE_END_TAG);
            if(split.length > 0) {
                split = split[0].split(MyEpisodeConstants.MYEPISODES_SEARCH_RESULT_PAGE_SPLITTER_TD_START_TAG);
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

    public void addShow(String myEpsidodesShowId, User user) throws InternetConnectivityException, LoginFailedException, UnsupportedHttpPostEncodingException, ShowAddFailedException {
        OkHttpClient httpClient = EpisodesService.getOkHttpClient();
        userService.login(httpClient, user.getUsername(), user.getPassword());
        String url = MyEpisodeConstants.MYEPISODES_ADD_SHOW_PAGE + myEpsidodesShowId;

        int status = 200;
        Request request = EpisodesService.buildRequest(httpClient, url);

        try {
            Response response = httpClient.newCall(request).execute();
        	status = response.code();
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Adding the show status failed for URL " + url;
            Log.w(LOG_TAG, message, e);
            throw new ShowAddFailedException(message, e);
        }

        if (status != 200) {
            String message = "Adding the show status failed with status code " + status + " for URL " + url;
            Log.w(LOG_TAG, message);
            throw new ShowAddFailedException(message);
        } else {
            Log.i(LOG_TAG, "Successfully added the show from url " + url);
        }
    }

    public List<Show> getFavoriteOrIgnoredShows(User user, ShowType showType) throws UnsupportedHttpPostEncodingException, InternetConnectivityException, LoginFailedException {
        OkHttpClient httpClient = EpisodesService.getOkHttpClient();
        userService.login(httpClient, user.getUsername(), user.getPassword());

        Request request = EpisodesService.buildRequest(httpClient, MyEpisodeConstants.MYEPISODES_FAVO_IGNORE_PAGE);

		String responsePage = "";
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            responsePage = response.body().string();
        } catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
		} catch (IOException e) {
            String message = "Search on MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new LoginFailedException(message, e);
        }

        List<Show> shows = parseShowsHtml(responsePage, showType);

        Log.d(LOG_TAG, shows.size() + " show(s) found!");

        return shows;
    }

    private List<Show> parseShowsHtml(String html, ShowType showType) {
        List<Show> shows = new ArrayList<Show>();

        String startTag = "<select id=\"";
        String endTag = "</select>";

        String optionStartTag = "<option value=\"";
        String optionEndTag = "</option>";

        switch(showType) {
            case FAVOURITE_SHOWS:
                startTag += "shows\"";
                break;
            case IGNORED_SHOWS:
                startTag += "ignored_shows\"";
                break;
        }
        int startPosition = html.indexOf(startTag);

        if(startPosition == -1) {
            return shows;
        }

        String selectTag = html.substring(startPosition, html.length());
        int endPosition = selectTag.indexOf(endTag);
        selectTag = selectTag.substring(0, endPosition);

        while(selectTag.length() > 0) {
            int startPosistionOption = selectTag.indexOf(optionStartTag);
            int endPositionOption = selectTag.indexOf(optionEndTag);

            if(startPosistionOption == -1 || endPositionOption == -1 || endPositionOption < startPosistionOption) {
                break;
            }

            String optionTag = selectTag.substring(startPosistionOption + optionStartTag.length(), endPositionOption);
            selectTag = selectTag.replace(optionStartTag+optionTag+optionEndTag, "");

            String[] values = optionTag.split("\">");
            if(values.length != 2) {
                break;
            }

            Show show = new Show(values[1].trim(), values[0].trim());
            shows.add(show);
            Log.d(LOG_TAG, "Show found: " + show.getShowName() + "(" + show.getMyEpisodeID() + ")");
        }

        return shows;
    }

    public List<Show> markShow(User user, Show show, ShowAction showAction, ShowType showType) throws LoginFailedException, InternetConnectivityException, UnsupportedHttpPostEncodingException {
        OkHttpClient httpClient = EpisodesService.getOkHttpClient();
        userService.login(httpClient, user.getUsername(), user.getPassword());

        String url = "";
        switch(showAction) {
            case IGNORE:
                Log.d(LOG_TAG, "IGNORING SHOWS");
                url = MyEpisodeConstants.MYEPISODES_FAVO_IGNORE_ULR;
                break;
            case UNIGNORE:
                Log.d(LOG_TAG, "UNIGNORING SHOWS");
                url = MyEpisodeConstants.MYEPISODES_FAVO_UNIGNORE_ULR;
                break;
            case DELETE:
                Log.d(LOG_TAG, "DELETING SHOWS");
                url = MyEpisodeConstants.MYEPISODES_FAVO_REMOVE_ULR;
                break;
        }

        Request request = EpisodesService.buildRequest(httpClient, url + show.getMyEpisodeID());

        try {
            httpClient.newCall(request).execute();
        } catch (UnknownHostException e) {
            String message = "Could not connect to host.";
            Log.e(LOG_TAG, message, e);
            throw new InternetConnectivityException(message, e);
        } catch (IOException e) {
            String message = "Markin shows on MyEpisodes failed.";
            Log.w(LOG_TAG, message, e);
            throw new LoginFailedException(message, e);
        }
        
        List<Show> shows = getFavoriteOrIgnoredShows(user, showType);

        return shows;
    }
}
