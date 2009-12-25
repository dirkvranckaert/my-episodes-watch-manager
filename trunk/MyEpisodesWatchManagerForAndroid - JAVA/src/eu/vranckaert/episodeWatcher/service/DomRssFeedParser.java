package eu.vranckaert.episodeWatcher.service;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.vranckaert.episodeWatcher.domain.Feed;
import eu.vranckaert.episodeWatcher.domain.FeedItem;
import eu.vranckaert.episodeWatcher.exception.FeedUrlParsingException;
import eu.vranckaert.episodeWatcher.exception.RssFeedParserException;

import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public class DomRssFeedParser implements RssFeedParser {
	private static final String LOG_TAG = "DomRssFeedParser";
	
    public Feed parseFeed(final URL url) throws RssFeedParserException, FeedUrlParsingException {
        Feed rssFeed = new Feed();
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
        	Log.e(LOG_TAG, e.getMessage());
            throw new RssFeedParserException(e);
        }
        
        try {
            doc = builder.parse(url.openStream());
        } catch (SAXException e) {
        	String message = "Unable to parse the URL for the feed";
        	Log.e(LOG_TAG, message);
            throw new FeedUrlParsingException(message, e);
        } catch (IOException e) {
        	String message = "Unable to parse the URL for the feed";
        	Log.e(LOG_TAG, message);
            throw new FeedUrlParsingException(message, e);
        }

        NodeList nodes = doc.getElementsByTagName("item");

        for (int i=0; i<nodes.getLength(); i++) {
            Node itemNode = nodes.item(i);

            NodeList contentNodes = itemNode.getChildNodes();
            FeedItem item = new FeedItem();
            for (int j=0; j<contentNodes.getLength(); j++) {
                Node contentNode = contentNodes.item(j);
                if (contentNode.getFirstChild() != null) {
                    String nodeValue = contentNode.getFirstChild().getNodeValue();
                    String nodeName = contentNode.getNodeName();
                    if (nodeName.equals("guid")) {
                        item.setGuid(nodeValue);
                    }
                    if (nodeName.equals("title")) {
                        item.setTitle(nodeValue);
                    }
                    if (nodeName.equals("link")) {
                        item.setLink(nodeValue);
                    }
                    if (nodeName.equals("description")) {
                        item.setDescription(nodeValue);
                    }
                }
            }

            rssFeed.addItem(item);
        }

        return rssFeed;
    }
}
