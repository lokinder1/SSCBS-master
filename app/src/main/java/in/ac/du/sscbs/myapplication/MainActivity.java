package in.ac.du.sscbs.myapplication;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {


    /*News Activity*/

    ConnectionDetector connectionDetector;
    Downloader downloader;
    ArrayList<String> Links;
    Context c = this;
    ArrayAdapter<String> adapter;
    ListView list;
    RequestQueue queue;
    LinkedHashMap<String, String> data;
    Stack<LinkedHashMap<String, String>> hashdata;
    final String URL = "http://www.sscbs.du.ac.in";
    SwipeRefreshLayout mSwipeRefreshLayout;
    Progress progress;
    ErrorDialogMessage errorDialogMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("News");
        setSupportActionBar(toolbar);
        connectionDetector = new ConnectionDetector(this);
        errorDialogMessage = new ErrorDialogMessage(this);


        if (!connectionDetector.isConnectingToInternet()) {


            errorDialogMessage.show();
        }


        progress = new Progress(this);
        progress.show();


        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloader = new Downloader(this);
        registerReceiver(downloader, filter);

        hashdata = new Stack<LinkedHashMap<String, String>>();
        queue = VolleySingleton.getInstance().getRequestQueue();
        list = (ListView) findViewById(R.id.lv_news);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initialrequest();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        list.setOnItemClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.rl_news);

        mSwipeRefreshLayout.setOnRefreshListener(this);


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (queue != null) {

            queue.cancelAll("News");
        }

        if (hashdata.size() > 1) {

            while (hashdata.size() != 1) {
                hashdata.pop();
            }


        }
    }

    @Override
    public void onBackPressed() {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (hashdata.size() > 1) {

            LinkedHashMap<String, String> popped = hashdata.pop();
            Links.clear();


            Set set = hashdata.peek().entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                Links.add(me.getKey().toString());
            }

            adapter.notifyDataSetChanged();


        } else {
            super.onBackPressed();
        }
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

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent = null;

        if (id == R.id.nav_login) {

            intent = new Intent(MainActivity.this,Login.class);
        } else if (id == R.id.nav_notices) {

            intent = new Intent(MainActivity.this, Notices.class);
        }
        else if (id == R.id.nav_time_table) {

            intent = new Intent(MainActivity.this, TimeTable.class);

        } else if (id == R.id.nav_aboutus) {

            intent = new Intent(MainActivity.this, AboutUs.class);
        }else if(id == R.id.nav_home){

            intent = new Intent(MainActivity.this, home.class);

        }else if(id == R.id.nav_cloud){

            intent = new Intent(MainActivity.this, cloud.class);

        }
        else if(id == R.id.nav_library){

            //intent = new Intent(MainActivity.this, library.class);

        }
        startActivity(intent);

        initialrequest();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        TextView tv = (TextView) view;

        String s = tv.getText().toString();
        secondRequest(s);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Links.clear();

        hashdata.clear();
        initialrequest();


    }


    void initialrequest() {


        final StringRequest firstReq = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Document d = Jsoup.parse(response);
                Elements mainPageLinks = d.select("div.gn_browser div.gn_news");

                Links = new ArrayList<String>();

                Elements redirectingLinks = mainPageLinks.select("a[href]");

                data = new LinkedHashMap<String, String>();

                for (Element temp : redirectingLinks) {


                    String tempText = temp.text();
                    if (tempText.length() > 0) {
                        Links.add(temp.text());
                        data.put(temp.text(), temp.attr("abs:href"));
                    }
                }


                hashdata.push(data);

                adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1, Links);
                list.setAdapter(adapter);
                progress.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {

                progress.stop();
                errorDialogMessage.show();


            }
        });

        firstReq.setTag("News");

        queue.add(firstReq);

    }

    void secondRequest(String s) {


        if (!hashdata.empty() && hashdata.size() == 1) {


            progress.show();

            LinkedHashMap<String, String> temp = hashdata.peek();

            String link = temp.get(s);

            final StringRequest linksRequest = new StringRequest(Request.Method.GET, link, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    data = new LinkedHashMap<String, String>();
                    Links.clear();
                    adapter.clear();

                    LinkedHashMap<String, String> temphash = new LinkedHashMap<String, String>();
                    Document d = Jsoup.parse(response);
                    Elements InsideLinks = d.select("div.item-page a[href]");

                    for (Element temp : InsideLinks) {

                        String t = temp.text();
                        if (t.length() > 0) {

                            Links.add(t);
                            temphash.put(t, temp.attr("abs:href"));
                        }
                    }


                    hashdata.push(temphash);
                    adapter.notifyDataSetChanged();
                    progress.stop();
                }


            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    progress.stop();
                    errorDialogMessage.show();
                }
            });


            linksRequest.setTag("News");
            queue.add(linksRequest);
        } else {


            if (!hashdata.empty()) {


                LinkedHashMap<String, String> temp = hashdata.peek();

                String link = temp.get(s);

                int length = link.length();

                int pointer = length - 1;
                int pointer1 = pointer - 1;
                int pointer2 = pointer1 - 1;

                if (connectionDetector.isConnectingToInternet()) {
                    if (link.charAt(pointer) == 'f' && link.charAt(pointer1) == 'd' && link.charAt(pointer2) == 'p') {

                        if (!downloader.download(link)) {

                            Toast.makeText(c, "Cant write on external", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(c, "Downloading", Toast.LENGTH_SHORT).show();

                        }


                    } else {

                        Intent openBrowser = new Intent(Intent.ACTION_VIEW);
                        openBrowser.setData(Uri.parse(link));
                        startActivity(openBrowser);
                    }
                } else {
                    errorDialogMessage.show();
                }

            }

        }


    }

    @Override
    public void onRefresh() {

        if (hashdata != null) {

            hashdata.clear();
            initialrequest();
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.progress_color_1, R.color.progress_color_3, R.color.progress_color_4, R.color.progress_color_5);
        } else {

            Toast.makeText(c, "Yet to fetch data", Toast.LENGTH_SHORT).show();
        }
    }
}
