package in.ac.du.sscbs.myapplication;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by baymax on 23/12/15.
 */
public class Progress {


    ProgressDialog Dialog;
    Context context;
    boolean ifstarted;

    Progress(Context c){

            ifstarted = false;
            context = c;
    }

    void show(){

        if(!ifstarted){


        Dialog = new ProgressDialog(context);
        Dialog.setTitle("Fetching Data");
        Dialog.setMessage("Please Wait.....");
        Dialog.setCancelable(true);
        Dialog.show();
        ifstarted = true;
        }

    }

    void stop(){

        if(ifstarted) {

            Dialog.cancel();
        }


        ifstarted = false;

    }
}