package in.ac.du.sscbs.myapplication;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.LinkedHashMap;
import java.util.Stack;

public class Notices extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ConnectionDetector connectionDetector;
    Downloader downloader;
    ArrayList<String> Links;
    Context c = this;
    ArrayAdapter<String> adapter;
    ListView list;
    RequestQueue queue;
    LinkedHashMap<String, String> data;
    Stack<LinkedHashMap<String, String>> hashdata;
    final String URL = "http://www.sscbs.du.ac.in/index.php/2014-01-16-07-34-49/2014-03-24-06-47-34";
    Progress progress;
    ErrorDialogMessage errorDialogMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notices);


        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);

        toolbar.setTitle("Notices");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connectionDetector = new ConnectionDetector(this);
        errorDialogMessage = new ErrorDialogMessage(this);


        if(!connectionDetector.isConnectingToInternet()){


            errorDialogMessage.show();
        }


        progress = new Progress(this);
        progress.show();

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloader = new Downloader(this);
        registerReceiver(downloader, filter);
        hashdata = new Stack<LinkedHashMap<String, String>>();
        queue = VolleySingleton.getInstance().getRequestQueue();
        list = (ListView) findViewById(R.id.lv_notices);

        final StringRequest firstReq = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Document d = Jsoup.parse(response);
                Elements links = d.select("div.item-page");

                Links = new ArrayList<String>();

                Elements downloadLink = links.select("a[href^=/files]");

                data = new LinkedHashMap<String, String>();

                for (Element temp : downloadLink) {


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

        firstReq.setTag("Notices");

        queue.add(firstReq);
        list.setOnItemClickListener(this);



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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



        TextView tv = (TextView) view;

        String s = tv.getText().toString();

        if (!hashdata.empty()) {


            LinkedHashMap<String, String> temp = hashdata.peek();

            String link = temp.get(s);

            int length = link.length();

            int pointer = length - 1;
            int pointer1 = pointer - 1;
            int pointer2 = pointer1 - 1;

            if(connectionDetector.isConnectingToInternet()){
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
            }else{
                errorDialogMessage.show();
            }

        }




    }
}
