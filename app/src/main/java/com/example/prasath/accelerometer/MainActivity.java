package com.example.prasath.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;


public class MainActivity extends Activity implements SensorEventListener {

    Sensor acc;
    SensorManager sm;
    TextView acceleration;
    private MobileServiceClient mClient;

    static float[] previous = new float[3];
    static float[] delta = new float[3];
    public static int jerkcount=0;
    static float jerk=0;
    static float[] weights = new float[3];
    static float firstorder=0;
    static float min, max;
    public static float potholemag =0;
    public static float roadmag=0;
    static float growth=0;
    static float decay=1;
    static boolean gyroPresent=true;
    static float velocity;
    Sampler roadSampler;
    public static Context c;
    public static float fromSampler=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        c = getApplicationContext();

        try {
            mClient = new MobileServiceClient(
                    "https://plotholesmobile.azurewebsites.net",
                    this
            );
        }
        catch(Exception e){

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroPresent = true;
        /*if(!sm.registerListener(this, acc, 100000)){
            acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, acc, 100000);
            gyroPresent=false;
        }*/

        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, acc, 100000);
        gyroPresent=false;

        acceleration=(TextView)findViewById(R.id.acceleration);

        Button tweak = (Button) findViewById(R.id.button);
        tweak.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                   EditText mEdit = (EditText) findViewById(R.id.editText4);
                   min = Float.valueOf(mEdit.getText().toString());
                   mEdit = (EditText) findViewById(R.id.editText3);
                   max = Float.valueOf(mEdit.getText().toString());
                   mEdit = (EditText) findViewById(R.id.editText2);
                   growth = Float.valueOf(mEdit.getText().toString());
                   mEdit = (EditText) findViewById(R.id.editText);
                   decay = Float.valueOf(mEdit.getText().toString());
                   jerkcount=0;
                   roadmag=0;
                   potholemag=0;
                   velocity=0;
                Toast toast = Toast.makeText(c, "Parameters tweaked!", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        roadSampler = new Sampler(this);
        roadSampler.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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



    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    public int getCount(){
        return jerkcount;
    }

    public void onSensorChanged(SensorEvent event){
        if(gyroPresent){
            handleLinearAcceleration(event);
        }
        else
            handleAccelerometer(event);

    }

    public void updateVelocity(SensorEvent event){
        float temp=0;
        for(int i=0; i<3; i++){
            temp+=event.values[i];
        }
        temp/=3;
        velocity+=(0.001)*temp;
    }
    public void handleLinearAcceleration(SensorEvent event){
        jerk = 0;

        updateVelocity(event);

        for(int i=0; i<3; i++){
            delta[i] = event.values[i] - previous[i];
            jerk+=(delta[i]*delta[i]);
            previous[i] = event.values[i];

        }
        jerk/=(1+Math.abs(velocity));
        if(jerk>max) {
            //jerkcount++;
            //Toast toast = Toast.makeText(getApplicationContext(), "Jerk magnitude : " + jerk, Toast.LENGTH_LONG);
            //toast.show();
            if(firstorder < min){
                firstorder=jerk;
                jerkcount++;
                //Toast toast = Toast.makeText(getApplicationContext(), "Jerk magnitude : " + jerk, Toast.LENGTH_LONG);
                //toast.show();
                roadmag+=potholemag/1000;
                potholemag=jerk/100;

                // call
            }
            else{
                firstorder+=growth;
                potholemag+=(jerk/100);
            }
        }
        else{
            firstorder/=decay;
            potholemag+=(jerk/100);

        }

        if(firstorder<min){
            firstorder = 0;
            //jerk = 0;
        }
        acceleration.setText("\nVelocity   " + velocity + "\n First order   " + firstorder+"\n Jerk  " + jerk +  "\n Pothole count  " + (jerkcount) + "\nX:   " + event.values[0] + "\nY:   " + event.values[1] + "\nZ:   " + event.values[2] + "\nroadmag:    " + roadmag);

    }

    public void handleAccelerometer(SensorEvent event){
        jerk = 0;

        for(int i=0; i<3; i++){
            delta[i] = event.values[i] - previous[i];
            jerk+=(delta[i]*delta[i])*weights[i]*weights[i];
            previous[i] = event.values[i];

        }
        jerk/=(1+Math.abs(velocity));
        if(jerk>max) {
            //jerkcount++;
            //Toast toast = Toast.makeText(getApplicationContext(), "Jerk magnitude : " + jerk, Toast.LENGTH_LONG);
            //toast.show();
            if(firstorder < min){
                firstorder=jerk;
                jerkcount++;
                getLocation();
                //Toast toast = Toast.makeText(getApplicationContext(), "Jerk magnitude : " + jerk, Toast.LENGTH_LONG);
                //toast.show();
                roadmag+=potholemag/1000;
                potholemag=jerk/100;

                // call
            }
            else{
                firstorder+=growth;
                potholemag+=(jerk/100);
            }
        }
        else{
            for(int i=0; i<3; i++)
                weights[i] = event.values[i];
            firstorder/=decay;
            potholemag+=(jerk/100);

        }

        if(firstorder<min){
            firstorder = 0;
            //jerk = 0;
        }
        acceleration.setText("\nfromSampler:   " + fromSampler + "\nVelocity   " + velocity + "\n First order   " + firstorder+"\n Jerk  " + jerk +  "\n Pothole count  " + (jerkcount) + "\nX:   " + event.values[0] + "\nY:   " + event.values[1] + "\nZ:   " + event.values[2] + "\nroadmag:    " + roadmag);
    }

    public void getLocation(){

    }

    public void databaseOperation(){
        //Toast toast = Toast.makeText(getApplicationContext(), "In dbop", Toast.LENGTH_LONG);
        //toast.show();
        final TodoItem item = new TodoItem();
        item.Text = "Awesome item";
        mClient.getTable(TodoItem.class).insert(item, new TableOperationCallback<TodoItem>() {
            public void onCompleted(TodoItem entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    // Insert succeeded
                    //Toast toast = Toast.makeText(getApplicationContext(), "Insertion succeeded", Toast.LENGTH_LONG);
                    //toast.show();
                } else {
                    // Insert failed
                    //Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_LONG);
                    //toast.show();
                }
            }
        });
    }


}
