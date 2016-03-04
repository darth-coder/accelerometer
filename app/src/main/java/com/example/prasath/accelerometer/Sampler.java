package com.example.prasath.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

/**
 * Created by prasath on 3/3/16.
 */
public class Sampler extends Thread {
    public MainActivity a;
    public Sampler(MainActivity a){
        super();
        this.a = a;
    }
    public void run(){
        while(true) {
            //Toast toast = Toast.makeText(MainActivity.c, "Sampled value: " + MainActivity.potholemag, Toast.LENGTH_LONG);
            //toast.show();
            a.fromSampler = a.potholemag;
            a.potholemag=0;
            a.databaseOperation();
            try {
                this.sleep(5000);
            }
            catch(Exception e){

            }

        }
    }
}
