package in.ac.du.sscbs.myapplication;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by baymax on 23/12/15.
 */
public class VolleySingleton {



    public static VolleySingleton sInstance = null;

    private RequestQueue mRequestQueue;

    private VolleySingleton() {

        mRequestQueue = Volley.newRequestQueue(MyApplication.getAppContext());
    }

    public static VolleySingleton getInstance() {

        if (sInstance == null) {

            sInstance = new VolleySingleton();
        }
        return sInstance;

    }

    public RequestQueue getRequestQueue() {

        return mRequestQueue;
    }
}
