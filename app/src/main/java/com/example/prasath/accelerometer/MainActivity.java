package com.example.prasath.accelerometer;

import android.app.Activity;
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


public class MainActivity extends Activity implements SensorEventListener {

    Sensor acc;
    SensorManager sm;
    TextView acceleration;

    static float[] previous = new float[3];
    static float[] delta = new float[3];
    static int jerkcount=0;
    static float jerk=0;
    static float[] weights = new float[3];
    static float firstorder=0;
    static float min, max;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, acc, 100000);

        acceleration=(TextView)findViewById(R.id.acceleration);

        Button tweak = (Button) findViewById(R.id.button);
        tweak.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                   EditText mEdit = (EditText) findViewById(R.id.editText);
                   min = Float.valueOf(mEdit.getText().toString());
                   mEdit = (EditText) findViewById(R.id.editText2);
                   max = Float.valueOf(mEdit.getText().toString());
                   jerkcount=0;
            }
        });
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
        jerk = 0;

        for(int i=0; i<3; i++){
            delta[i] = event.values[i] - previous[i];
            jerk+=(delta[i]*delta[i])*weights[i]*weights[i];
            previous[i] = event.values[i];

        }
        if(jerk>max) {
            //jerkcount++;
            //Toast toast = Toast.makeText(getApplicationContext(), "Jerk magnitude : " + jerk, Toast.LENGTH_LONG);
            //toast.show();
            if(firstorder < 3000){
                firstorder=jerk;
                jerkcount++;
                // call
            }
            else{
                firstorder+=min;
            }
        }
        else{
            for(int i=0; i<3; i++)
                weights[i] = event.values[i];
            firstorder/=2;

        }

        if(firstorder<min){
            firstorder = 0;
            //jerk = 0;
        }
        acceleration.setText("\n" + firstorder+"\n" + jerk +  "\n" + (jerkcount) + "\n" + event.values[0] + "\n " + event.values[1] + "\n " + event.values[2]);
    }
}
