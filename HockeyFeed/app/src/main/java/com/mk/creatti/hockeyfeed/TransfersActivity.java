package com.mk.creatti.hockeyfeed;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mikko on 9.4.2015.
 */
public class TransfersActivity  extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfers);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.transfers_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if ( id == R.id.action_refresh){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class PlaceholderFragment extends ListFragment {
        public List<String> Content = new ArrayList<String>();

        public PlaceholderFragment() {
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            ListView lv = getListView();
            lv.setTextFilterEnabled(true);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(view.getContext(), ((TextView) view).getText(),
                            Toast.LENGTH_SHORT).show();

                    // Next rows are for future possibility to view more info about the article without using browser

                   // Intent articleContent = new Intent().setClass(PlaceholderFragment.this.getActivity(), ArticleView.class);
                   // articleContent.putExtra("article number", i);
                   // articleContent.putExtra("article content", Content.get(i));
                   // startActivity(articleContent);
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            new GetAndroidPitRssFeedTask().execute();
        }

        private String getAndroidPitRssFeed() throws IOException {
            InputStream in = null;
            String rssFeed = null;
            try {
                URL url = new URL("http://www.eliteprospects.com/rss_league.php?league=6");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                for (int count; (count = in.read(buffer)) != -1; ) {
                    out.write(buffer, 0, count);
                }
                byte[] response = out.toByteArray();
                rssFeed = new String(response, "ISO-8859-1");
            } finally {
                if (in != null) {
                    in.close();
                }
            }
            return rssFeed;
        }

        private class GetAndroidPitRssFeedTask extends AsyncTask<Void, Void, List<String>> {

            @Override
            protected List<String> doInBackground(Void... voids) {
                List<String> result = null;
                try {
                    String feed = getAndroidPitRssFeed();
                    result = parse(feed);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            private List<String> parse(String rssFeed) throws XmlPullParserException, IOException {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(rssFeed));
                xpp.nextTag();
                return readRss(xpp);
            }

            // This function parses XML based on the keyword "rss"
            // and feeds each "channel" into a List<String> items
            // readChannel parses the channels
            private List<String> readRss(XmlPullParser parser)
                    throws XmlPullParserException, IOException {
                List<String> items = new ArrayList<String>();
                parser.require(XmlPullParser.START_TAG, null, "rss");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("channel")) {
                        items.addAll(readChannel(parser));
                    } else {
                        skip(parser);
                    }
                }
                return items;
            }

            // Take each "channel" and parses based on "item"
            // Feeds each "item" into readItem
            private List<String> readChannel(XmlPullParser parser)
                    throws IOException, XmlPullParserException {

                List<String> items = new ArrayList<String>();
                parser.require(XmlPullParser.START_TAG, null, "channel");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("item")) {
                        items.add(readItem(parser));
                    } else {
                        skip(parser);
                    }
                }
                return items;
            }

            // Takes each "item" and parses based on "title"
            // Feeds the title into the readTitle, to return the title of an article
            private String readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
                String result = null;
                parser.require(XmlPullParser.START_TAG, null, "item");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("title")) {
                        result = readTitle(parser);
                    } else if (name.equals("content:encoded")) {
                        Content.add(readContent(parser));
                    } else {
                        skip(parser);
                    }
                }
                return result;
            }
            // Processes title tags in the feed.


            private String readTitle(XmlPullParser parser)
                    throws IOException, XmlPullParserException {
                parser.require(XmlPullParser.START_TAG, null, "title");
                String title = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "title");
                return title;
            }

            // Processes content tags in the feed
            private String readContent(XmlPullParser parser)
                    throws IOException, XmlPullParserException {
                parser.require(XmlPullParser.START_TAG, null, "content:encoded");
                String content = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "content:encoded");
                return content;
            }

            // Gets the actual text in XML section and returns it
            // This function is called for readTitle and any others that need it
            // (AKA readContent, readImage, etc.)
            private String readText(XmlPullParser parser)
                    throws IOException, XmlPullParserException {
                String result = "";
                if (parser.next() == XmlPullParser.TEXT) {
                    result = parser.getText();
                    parser.nextTag();
                }
                return result;
            }

            // This function purposefully ignores any tags that we don't care about
            private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    throw new IllegalStateException();
                }
                int depth = 1;
                while (depth != 0) {
                    switch (parser.next()) {
                        case XmlPullParser.END_TAG:
                            depth--;
                            break;
                        case XmlPullParser.START_TAG:
                            depth++;
                            break;
                    }
                }
            }

            // This function sets the list adapter and populates it with rssFeed
            @Override
            protected void onPostExecute(List<String> rssFeed) {
                if (rssFeed != null) {
                    setListAdapter(new ArrayAdapter<String>(
                            getActivity(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            rssFeed));
                }
            }
        }
    }
}
