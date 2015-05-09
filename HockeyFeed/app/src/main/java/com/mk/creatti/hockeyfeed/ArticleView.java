package com.mk.creatti.hockeyfeed;

/**
 * Created by Mikko on 5.4.2015.
 */

/*public class ArticleView extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.containerarticle, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_article_view, container, false);

            Intent articleContent = this.getActivity().getIntent();
            int articleNumber = articleContent.getIntExtra("article number",0);
            String content = articleContent.getStringExtra("article content");

            setContentView(R.layout.fragment_article_view);

            TextView tv = (TextView) findViewById(R.id.article_content);
            tv.setText(content);
            return rootView;
        }
    }

}
*/