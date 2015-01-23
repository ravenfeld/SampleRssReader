/*
 * Copyright (c) 2015. - Alexis Lecanu (alexis.lecanu@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package fr.alecanu.samplerssreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;


public class RssService extends AsyncTask<String, Void, Boolean> {

    private static final String UTF_8 = "UTF_8";
    private static final String TAG_ITEM = "item";
    private static final String TAG_TITLE = "title";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_PUB_DATE = "pubDate";
    private static final String TAG_CONTENT_ENCODED = "encoded";

    private final RssDataRetriever mDataRetriever;
    private final Context mContext;

    public static interface RssDataRetriever {

        public void onPreparedRss();

        public void onDataReceivedRss();

        public void onDataReceivedRssError();
    }

    public RssService(Context context, RssDataRetriever dataRetriever) {
        mDataRetriever = dataRetriever;
        mContext = context;

    }

    protected void onPreExecute() {
        if (mDataRetriever != null)
            mDataRetriever.onPreparedRss();
    }


    protected void onPostExecute(Boolean success) {
        if (mDataRetriever != null) {
            if (success) {
                mDataRetriever.onDataReceivedRss();
            } else {
                mDataRetriever.onDataReceivedRssError();
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (mDataRetriever != null)
            mDataRetriever.onDataReceivedRssError();
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        Database dba = new Database(mContext);
        dba.deleted();
        dba.openToWrite();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            URL url = new URL(urls[0]);
            URLConnection connection  = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            xpp.setInput(connection.getInputStream(), UTF_8);
            int eventType = xpp.getEventType();
            Article feed = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase(TAG_ITEM)) {
                        feed = new Article();
                    } else if (feed != null && xpp.getName().equalsIgnoreCase(TAG_TITLE)) {
                        feed.setTitle(xpp.nextText());
                    } else if (feed != null && xpp.getName().equalsIgnoreCase(TAG_DESCRIPTION)) {
                        feed.setDescription(xpp.nextText());
                    } else if (feed != null && xpp.getName().equalsIgnoreCase(TAG_PUB_DATE)) {
                        feed.setDate(xpp.nextText());
                    } else if (feed != null && xpp.getName().equalsIgnoreCase(TAG_CONTENT_ENCODED)) {
                        feed.setEncodedContent(xpp.nextText());
                    }
                } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase(TAG_ITEM)) {
                    dba.insertArticle(feed);
                }
                eventType = xpp.next();
            }
            return true;
        } catch (XmlPullParserException|IOException e) {
            e.printStackTrace();
        } finally {
            dba.close();
        }

        return false;
    }
}