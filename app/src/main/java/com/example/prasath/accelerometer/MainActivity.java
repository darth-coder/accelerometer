package com.example.prasath.accelerometer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener {

    Sensor acc;
    SensorManager sm;
    TextView acceleration;

    static float[] previous = new float[3];
    static float[] delta = new float[3];
    static int jerkcount=0;
    static float jerk=0;
    static float[] weights = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);

        acceleration=(TextView)findViewById(R.id.acceleration);


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

    public void onSensorChanged(SensorEvent event){
        jerk = 0;

        for(int i=0; i<3; i++){
            delta[i] = event.values[i] - previous[i];
            jerk+=(delta[i]*delta[i])*weights[i]*weights[i];
            previous[i] = event.values[i];

        }
        if(jerk>5000)
            jerkcount++;
        else{
            for(int i=0; i<3; i++)
                weights[i] = event.values[i];
        }
        acceleration.setText("\n" + jerk + "\n" + (jerkcount/3) + "\n" + event.values[0] + "\n " + event.values[1] + "\n " + event.values[2]);
    }
}
