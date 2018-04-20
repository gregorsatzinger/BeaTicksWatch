package com.example.tobias.beaticks;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    static final UUID MY_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");
    private ConstraintLayout mContainerView;
    private ConstraintLayout mainLayout;
    private ConstraintLayout BLDevices;
    private TextView value;
    private Button bConnect;
    private TextView device1;
    private TextView device2;
    private TextView device3;
    private TextView device4;
    private TextView connectionFailed;
    private String text;
    private ViewSwitcher viewSwitch;

    private int countDevice = 0;

    BluetoothSocket socket;
    BluetoothServerSocket serverSocket;
    BluetoothDevice[] currentDevice;
    BluetoothAdapter adapter;
    BluetoothManager manager;
    OutputStream out;
    UUID uuids;
    int iBpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (ConstraintLayout) findViewById(R.id.container);
        BLDevices = (ConstraintLayout) findViewById(R.id.BLDevices);
        mainLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        value = (TextView) findViewById(R.id.value);
        bConnect = (Button) findViewById(R.id.bConnect);
        viewSwitch = (ViewSwitcher) findViewById(R.id.viewSwitch);

        device1 = (TextView) findViewById(R.id.device1);
        device2 = (TextView) findViewById(R.id.device2);
        device3 = (TextView) findViewById(R.id.device3);
        device4 = (TextView) findViewById(R.id.device4);
        connectionFailed = (TextView)findViewById(R.id.connectionFailed);
        connectionFailed.setText("Not Connected");
        connectionFailed.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


       // viewSwitch.showNext();

        SensorManager mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
      //  System.out.println(uuids.toString());


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String msg = "" + (int) event.values[0];
            iBpm = (int)event.values[0];
            value.setText(msg);

            if(connectionFailed.getText() == "Connected")
                Send(iBpm);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("da", "onAccuracyChanged - accuracy: " + accuracy);
    }

    public void bConnectToDevice(View view) {

        if(socket != null && socket.isConnected()){
            bConnect.setText("Connect");
            connectionFailed.setText("Not Connected");

            try {
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            bConnect.setText("Disconnect");

            adapter = BluetoothAdapter.getDefaultAdapter();

            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            countDevice = 0;
            currentDevice = pairedDevices.toArray(new BluetoothDevice[0]);
            if (currentDevice.length >= (countDevice + 1)) {
                device1.setText("" + currentDevice[countDevice].getName());
                countDevice++;
            }
            if (currentDevice.length >= (countDevice + 1)) {
                device2.setText("" + currentDevice[countDevice].getName());
                countDevice++;
            }
            if (currentDevice.length >= (countDevice + 1)) {
                device3.setText("" + currentDevice[countDevice].getName());
                countDevice++;
            }
            if (currentDevice.length >= (countDevice + 1)) {
                device4.setText("" + currentDevice[countDevice].getName());
                countDevice++;
            }

            countDevice = -1;
            viewSwitch.showNext();
        }

    }

    public void Send(int bpm){
        try{

            out.write(bpm);
            out.flush();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void bSelectDevice(View view){
        if(view.getId() == R.id.device1){
            countDevice = 0;
        } else if(device2.performLongClick() == true){
            countDevice = 1;
        } else if(device3.performLongClick() == true){
            countDevice = 2;
        } else if(device4.performLongClick() == true){
            countDevice = 3;
        }

        try {
            if(socket != null && socket.isConnected()){
                bConnect.setText("Connect");

                socket.close();
            } else {
                viewSwitch.showPrevious();
                bConnect.setText("Disconnect");
                socket = currentDevice[countDevice].createInsecureRfcommSocketToServiceRecord(MY_UUID);

                socket.connect();

                out = socket.getOutputStream();
                connectionFailed.setText("Connected");
            }
        } catch(Exception e) {

            bConnect.setText("Connect");

            connectionFailed.setText("Connection Failed");
            e.printStackTrace();
        }
    }
}
