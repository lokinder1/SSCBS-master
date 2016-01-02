package in.ac.du.sscbs.myapplication;

import android.graphics.Bitmap;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TimeTable extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {



    ConnectionDetector connectionDetector;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ErrorDialogMessage errorDialogMessage;
    boolean loadingFinished = true;
    boolean redirect = false;
    Progress progress;

    public WebView Wv;
    final  String url = "http://collegeprojects.net.in/att_sscbs/main/Student/index.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        progress = new Progress(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Time Table");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Wv = (WebView) findViewById(R.id.wv_timetable);
        WebSettings WebSettings = Wv.getSettings();
        WebSettings.setJavaScriptEnabled(true);

        connectionDetector = new ConnectionDetector(this);
        errorDialogMessage = new ErrorDialogMessage(this);
        if (!connectionDetector.isConnectingToInternet()) {


            errorDialogMessage.show();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rl_time_table);

        mSwipeRefreshLayout.setOnRefreshListener(this);




        Wv.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                view.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                loadingFinished = false;
                progress.show();
                 }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!redirect) {
                    loadingFinished = true;
                }

                if (loadingFinished && !redirect) {

                    progress.stop();

                   } else {
                    redirect = false;
                }

            }
        });
        Wv.loadUrl(url);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;


        }

        if (id == android.R.id.home) {

            NavUtils.navigateUpFromSameTask(this);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

        String url = Wv.getUrl();
        Wv.loadUrl(url);
        mSwipeRefreshLayout.setRefreshing(false);

    }
}
