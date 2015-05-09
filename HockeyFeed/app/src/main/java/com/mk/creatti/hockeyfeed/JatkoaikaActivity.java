package com.mk.creatti.hockeyfeed;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
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
 * Created by Mikko on 19.4.2015.
 */
public class JatkoaikaActivity extends ActionBarActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_jatkoaika);

            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.container3, new PlaceholderFragment())
                        .commit();
            }
        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.jatkoaika_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();
            if (id == R.id.action_settings) {
                return true;
            }
            if ( id == R.id.action_refresh){

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


                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jatkoaika.com"));
                        startActivity(browserIntent);
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
                    URL url = new URL("http://www.jatkoaika.com/rss/index.rss");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    in = conn.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int count; (count = in.read(buffer)) != -1; ) {
                        out.write(buffer, 0, count);
                    }
                    byte[] response = out.toByteArray();
                    rssFeed = new String(response, "utf-8");
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

                private String readTitle(XmlPullParser parser)
                        throws IOException, XmlPullParserException {
                    parser.require(XmlPullParser.START_TAG, null, "title");
                    String title = readText(parser);
                    parser.require(XmlPullParser.END_TAG, null, "title");
                    return title;
                }

                private String readContent(XmlPullParser parser)
                        throws IOException, XmlPullParserException {
                    parser.require(XmlPullParser.START_TAG, null, "content:encoded");
                    String content = readText(parser);
                    parser.require(XmlPullParser.END_TAG, null, "content:encoded");
                    return content;
                }

                private String readText(XmlPullParser parser)
                        throws IOException, XmlPullParserException {
                    String result = "";
                    if (parser.next() == XmlPullParser.TEXT) {
                        result = parser.getText();
                        parser.nextTag();
                    }
                    return result;
                }

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
