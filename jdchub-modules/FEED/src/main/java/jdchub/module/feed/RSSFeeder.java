/*
* RSSFeeder.java
*
* Created on 09 02 2012, 15:43
*
* Copyright (C) 2012 Alexey 'lh' Antonov
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package jdchub.module.feed;

import com.adamtaft.eb.EventBusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import ru.sincore.Broadcast;
import ru.sincore.ClientManager;
import ru.sincore.ConfigurationManager;
import ru.sincore.events.NewRssFeedEvent;
import ru.sincore.util.AdcUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;

/**
 * Rss feeder (works with RSS feed only, Atom not supported yet)
 *
 * @author Alexey 'lh' Antonov
 * @since 2012-02-09
 */
public class RSSFeeder extends TimerTask
{
    private static final Logger log = LoggerFactory.getLogger(RSSFeeder.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    private URL feedURL = null;
    private String feedName = null;
    private String feedDescription = null;
    
    private String lastFeedTitle = null;

    public RSSFeeder(URL feedURL)
    {
        this.feedURL = feedURL;
    }


    private String getRssPostField(Document doc, XPath xpath, String fieldName)
            throws XPathExpressionException
    {
        XPathExpression expr = xpath.compile("/rss/channel/item[1]/" + fieldName);
        return  (String) expr.evaluate(doc, XPathConstants.STRING);
    }


    /**
     * Converts date from locale-sensitive manner to long.<br/>
     * If can't convert, return current time by calling System.currentTimeMillis() function.
     * 
     * @param dateString date in locale-sensitive manner (For example, "Thu, 09 Feb 2012 03:57:39 GMT")
     * @return time in millis 
     */
    private long convertLocaleSensitiveDateToLong(String dateString)
    {
        Date date = null;
        
        try
        {
            date = dateFormat.parse(dateString);
        }
        catch (ParseException e)
        {
            log.error(e.toString());
            return System.currentTimeMillis();
        }

        return date.getTime();
    }


    /**
     * Get last post from RSS feed.
     *
     * @return rss feed event, if new post recieved
     */
    private NewRssFeedEvent getLastPost()
    {
        NewRssFeedEvent rssFeedEvent = new NewRssFeedEvent();

        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(feedURL.openStream());

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();


            if (feedName == null)
            {
                XPathExpression expr = xpath.compile("/rss/channel/title");
                feedName = (String) expr.evaluate(doc, XPathConstants.STRING);
            }
            
            if (feedDescription == null)
            {
                XPathExpression expr = xpath.compile("/rss/channel/description");
                feedDescription = (String) expr.evaluate(doc, XPathConstants.STRING);
            }

            rssFeedEvent.setFeedName(feedName);
            rssFeedEvent.setFeedDescription(feedDescription);

            // TODO [lh] change to normal author name parsing (Error #208)
            rssFeedEvent.setAuthorName(getRssPostField(doc, xpath, "author"));
            rssFeedEvent.setPostName(getRssPostField(doc, xpath, "title"));
            rssFeedEvent.setPostDescription(getRssPostField(doc, xpath, "description"));
            rssFeedEvent.setLink(getRssPostField(doc, xpath, "link"));

            rssFeedEvent.setPublishTime(convertLocaleSensitiveDateToLong(getRssPostField(doc, xpath, "pubDate")));
            rssFeedEvent.setEditTime(convertLocaleSensitiveDateToLong(getRssPostField(doc, xpath, "editDate")));
        }
        catch (Exception e)
        {
            log.error(e.toString());
            return null;
        }

        return rssFeedEvent;
    }


    private void publishNewRssFeed()
    {
        StringBuilder message = new StringBuilder();

        message.append("IRSS");
        message.append(" ");
        message.append(AdcUtils.toAdcString(this.feedURL.toString()));
        message.append(" ");

        // add feed name
        message.append("FN");
        message.append(AdcUtils.toAdcString(this.feedName));
        message.append(" ");

        // add feed description
        message.append("FD");
        message.append(AdcUtils.toAdcString(this.feedDescription));
        message.append(" ");

        Broadcast.getInstance().broadcast(message.toString(), ClientManager.getInstance().getClientBySID(
                ConfigurationManager.instance().getString(ConfigurationManager.HUB_SID)));
    }



    private void sendNewRssPostMessage(NewRssFeedEvent rssFeedEvent)
    {
        StringBuilder message = new StringBuilder();

        message.append("IRSS");
        message.append(" ");
        message.append(AdcUtils.toAdcString(this.feedURL.toString()));
        message.append(" ");

        // add title
        message.append("TI");
        message.append(AdcUtils.toAdcString(rssFeedEvent.getPostName()));
        message.append(" ");

        // add description
        message.append("DE");
        message.append(AdcUtils.toAdcString(rssFeedEvent.getPostDescription()));
        message.append(" ");

        // add link to post
        message.append("LI");
        message.append(AdcUtils.toAdcString(rssFeedEvent.getLink()));
        message.append(" ");

        // add post date
        message.append("DT");
        message.append(AdcUtils.toAdcString(Long.toString(rssFeedEvent.getPublishTime())));
        message.append(" ");

        // add post author
        message.append("CR");
        message.append(AdcUtils.toAdcString(rssFeedEvent.getAuthorName()));
        message.append(" ");

        Broadcast.getInstance().broadcast(message.toString(), ClientManager.getInstance().getClientBySID(
                ConfigurationManager.instance().getString(ConfigurationManager.HUB_SID)));
    }


    @Override
    public void run()
    {
        try
        {
            NewRssFeedEvent rssFeedEvent = this.getLastPost();

            if (rssFeedEvent == null)
            {
                return;
            }

            if (lastFeedTitle == null)
            {
                this.publishNewRssFeed();
            }

            if (rssFeedEvent.getPostName().equals(lastFeedTitle))
            {
                return;
            }

            lastFeedTitle = rssFeedEvent.getPostName();

            EventBusService.publish(rssFeedEvent);

            sendNewRssPostMessage(rssFeedEvent);
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
    }
}
