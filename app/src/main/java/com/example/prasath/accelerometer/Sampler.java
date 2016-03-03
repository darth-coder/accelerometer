package com.example.prasath.accelerometer;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by prasath on 3/3/16.
 */
public class Sampler extends Thread {
    public Context context;
    /*public Sampler(Context c){
        super();
        context = c;
    }*/
    public void run(){
        while(true) {
            //Toast toast = Toast.makeText(MainActivity.c, "Sampled value: " + MainActivity.potholemag, Toast.LENGTH_LONG);
            //toast.show();
            MainActivity.fromSampler = MainActivity.potholemag;
            try {
                this.sleep(2000);
            }
            catch(Exception e){

            }

        }
    }
}
