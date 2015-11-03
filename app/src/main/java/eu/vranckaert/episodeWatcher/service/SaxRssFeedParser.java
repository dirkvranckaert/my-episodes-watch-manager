package eu.vranckaert.episodeWatcher.service;

import android.util.Log;
import eu.vranckaert.episodeWatcher.constants.MyEpisodeConstants;
import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.domain.FeedItem;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.RssFeedParserException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;

public class SaxRssFeedParser extends DefaultHandler implements RssFeedParser {
	private static final String LOG_TAG = SaxRssFeedParser.class.getSimpleName();
	
    boolean inItem = false;
    boolean inDescription = true;
    boolean recordTitleString = false;
    StringBuilder tempTitle = new StringBuilder();
    StringBuilder nodeValue = null;
    Feed feed = new Feed();
    FeedItem item = null;

    public Feed parseFeed(final URL url) throws ParserConfigurationException, SAXException, FeedUrlParsingException, RssFeedParserException, InternetConnectivityException {
        InputStream inputStream;
		try {
			
			//Log.e(LOG_TAG, "URL???: " + url.toString().substring(0, 16));
			if(url.toString().substring(0, 16).equalsIgnoreCase("http://127.0.0.1")){
				inputStream = new ByteArrayInputStream(MyEpisodeConstants.EXTENDED_EPISODES_XML.getBytes("UTF-8"));
			}else{
				inputStream = url.openConnection().getInputStream();
			}
		} catch (UnknownHostException e) {
			String message = "Could not connect to host.";
			Log.e(LOG_TAG, message, e);
			throw new InternetConnectivityException(message, e);
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

        //Log.d(LOG_TAG, " Feed size: " + feed.getItems().size());
        
        return feed;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals("item")) {
            item = new FeedItem();
            item.setTitle("");
            inItem = true;
            inDescription = false;
        } else if(localName.equals("description")) {
            //Fix for issue 96: If we ignore the description tag the issue is solved!
            inDescription = true;
            nodeValue = null;
        } else {
            inDescription = false;
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
            if (nodeValue != null) {
                item.setLink(nodeValue.toString());
            }
        }
        if (inItem && localName.equals("description")) {
            item.setDescription("");
        }
        nodeValue = null;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        //Fix for issue 96: If we ignore the description tag the issue is solved!
        if(!inDescription) {
            nodeValue = new StringBuilder(new String(ch, start, length));
            if(recordTitleString)
            {
                if (nodeValue.length() == 1 && nodeValue.toString().equals("]"))
                {
                    tempTitle.append(nodeValue);
                    recordTitleString = false;
                }
                else if (nodeValue.length() > 1 && nodeValue.substring(nodeValue.length()-2,nodeValue.length()).equals(" ]"))
                {
                    tempTitle.append(nodeValue);
                    recordTitleString = false;
                }
                else
                {
                    tempTitle.append(nodeValue);
                }
            }
            else
            {
                if (nodeValue.length() > 1 && nodeValue.substring(0,2).equals("[ ")) {
                    if(nodeValue.length() > 1 && nodeValue.substring(nodeValue.length()-2,nodeValue.length()).equals(" ]"))
                    {
                        tempTitle.append(nodeValue);
                    }
                    else
                    {
                        tempTitle.append(nodeValue);
                        recordTitleString = true;
                    }
                }
            }
        }
    }
}
