package eu.vranckaert.episodeWatcher.service;

import java.net.URL;

import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.RssFeedParserException;


public interface RssFeedParser {
	public Feed parseFeed(final URL url) throws Exception, RssFeedParserException, FeedUrlParsingException, InternetConnectivityException;
}
