package in.ac.du.sscbs.myapplication;

import android.app.Application;
import android.content.Context;

/**
 * Created by baymax on 23/12/15.
 */
public class MyApplication extends Application {




    public static MyApplication sInstance;

    @Override
    public void onCreate() {

        super.onCreate();
        sInstance = this;
    }


    public static Context getAppContext(){

        return sInstance.getApplicationContext();
    }


}
