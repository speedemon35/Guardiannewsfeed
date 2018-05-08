package newsfeed.guardian;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements android.app.LoaderManager.LoaderCallbacks<List<News>> {
    private static int LOADER_ID = 0;

    private TextView mEmptyStateTextView;
    private NewsAdapter adapter;
    private String guardian_url = "http://content.guardianapis.com/search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ListView listView = findViewById(R.id.list_view);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        listView.setEmptyView(mEmptyStateTextView);
        adapter = new NewsAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News news = adapter.getItem(i);
                String url = news.url;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(LOADER_ID, null, this);
        } else {
            // Otherwise, display error

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    //////////////////////////
    //http://open-platform.theguardian.com/explore/
    //////////////////////////
    //http://content.guardianapis.com/search?order-by=relevance&q=Politics&api-key=034d036d-2c1f-4594-9188-c01aa58ba898
    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<News>> onCreateLoader(int i, Bundle args) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String relevance = sharedPrefs.getString(
                getString(R.string.settings_relevance_key),
                getString(R.string.settings_relevance_default));

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(guardian_url);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=geojson`
        //appendquery needs to be modified to account for preferences but guaridan API seems to be down so can't fix/check
        uriBuilder.appendQueryParameter("order-by", relevance);
        uriBuilder.appendQueryParameter("show-references", "author");
        uriBuilder.appendQueryParameter("show-tags", "contributor");//second input is my own variable warning
        uriBuilder.appendQueryParameter("q","Politics");
        uriBuilder.appendQueryParameter("api-key","034d036d-2c1f-4594-9188-c01aa58ba898");
        uriBuilder.appendQueryParameter("orderby", "orderBy");//second input is my own variable warning

        // Return the completed uri
        return new NewsLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {

        // Set empty state text to display "No news found."
        mEmptyStateTextView.setText(R.string.no_news);

        if (data != null) {
            adapter.setNotifyOnChange(false);
            adapter.clear();
            adapter.setNotifyOnChange(true);
            adapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {

    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
