package be.dvwd.episodeWatcher.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import be.dvwd.episodeWatcher.domain.Feed;
import be.dvwd.episodeWatcher.domain.FeedItem;
import be.dvwd.episodeWatcher.exception.FeedUrlParsingException;
import be.dvwd.episodeWatcher.exception.RssFeedParserException;

public class SaxRssFeedParser extends DefaultHandler implements RssFeedParser {
	private static final String LOG_TAG = "SaxRssFeedParser";
	
    boolean inItem = false;
    StringBuilder tempTitle = new StringBuilder();
    StringBuilder nodeValue = null;
    Feed feed = new Feed();
    FeedItem item = null;

    public Feed parseFeed(final URL url) throws ParserConfigurationException, SAXException {
        InputStream inputStream;
		try {
			inputStream = url.openConnection().getInputStream();
		} catch (IOException e) {
			String message = "Could not parse the URL for the feed.";
			Log.e(LOG_TAG, message, e);
			throw new FeedUrlParsingException(message, e);
		}

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxFactory.newSAXParser();
        
        try {
			parser.parse(inputStream, this);
		} catch (SAXException e) {
			String message = "Exception occured during the RSS parsing process.";
			Log.e(LOG_TAG, message, e);
			throw new RssFeedParserException(message, e);
		} catch (IOException e) {
			String message = "Exception occured during the RSS parsing process.";
			Log.e(LOG_TAG, message, e);
			throw new RssFeedParserException(message, e);
		}

        Log.d(LOG_TAG, "Feed size: " + feed.getItems().size());
        
        return feed;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("item")) {
            item = new FeedItem();
            item.setTitle("");
            inItem = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("item")) {
            feed.addItem(item);
            item = null;
            inItem = false;
        }

        if (inItem && localName.equals("guid")) {
            item.setGuid(nodeValue.toString());
        }
        if (inItem && localName.equals("title")) {
            item.setTitle(tempTitle.toString());
            tempTitle = new StringBuilder();
        }
        if (inItem && localName.equals("link")) {
            item.setLink(nodeValue.toString());
        }
        if (inItem && localName.equals("description")) {
            item.setDescription(""); //TODO check why only the char '<' is placed in the descirption (something with html in the RSS feed!)
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        nodeValue = new StringBuilder(new String(ch, start, length));
        if (nodeValue.length() > 1 && nodeValue.substring(0,2).equals("[ ")) {
            tempTitle.append(nodeValue);
        }
    }
}
