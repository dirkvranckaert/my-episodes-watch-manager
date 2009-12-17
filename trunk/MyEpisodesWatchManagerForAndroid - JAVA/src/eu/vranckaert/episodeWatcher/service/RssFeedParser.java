package eu.vranckaert.episodeWatcher.service;

import java.net.URL;

import eu.vranckaert.episodeWatcher.domain.Feed;


public interface RssFeedParser {
	public Feed parseFeed(final URL url) throws Exception;
}
