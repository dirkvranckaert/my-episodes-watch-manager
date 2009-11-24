package be.dvwd.episodeWatcher.service;

import java.net.URL;

import be.dvwd.episodeWatcher.domain.Feed;

public interface RssFeedParser {
	public Feed parseFeed(final URL url) throws Exception;
}
