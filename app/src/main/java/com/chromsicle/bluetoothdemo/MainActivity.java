package com.chromsicle.bluetoothdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    TextView statusTextView;
    Button searchButton;
    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    //ArrayList used to filter out duplicate device with different dBm
    ArrayList<String> addresses = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        statusTextView = findViewById(R.id.statusTextView);
        searchButton = findViewById(R.id.searchButton);

        //set up the array
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, bluetoothDevices);
        listView.setAdapter(arrayAdapter);

        //allows us to work with this bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //create an intent filter so we can manage what we're actually looking for
        IntentFilter intentFilter = new IntentFilter();

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND); //when a new device is discovered
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter); //broadcastReceiver is created below
        //in order for the intent filter to work you have to register it


        //start searching for Bluetooth devices, don't forget to ask for permission in the manifest
        bluetoothAdapter.startDiscovery();
    }

    public void searchButtonClicked(View view) {
        statusTextView.setText("Searching...");
        //disable the button so the launch process isn't started again for a moment
        searchButton.setEnabled(false);
        //clear the list whenever the button is pressed
        bluetoothDevices.clear();
        addresses.clear();
        //start searching for Bluetooth devices, don't forget to ask for permission in the manifest
        bluetoothAdapter.startDiscovery();
    }

    //used for the intent registration stuff
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("Action", action);

            //re-enable the disabled button
            if(bluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //the search finished
                statusTextView.setText("Finished");
                //disable the button so the launch process isn't started again for a moment
                searchButton.setEnabled(true);
            } else if ((BluetoothDevice.ACTION_FOUND.equals(action))) {
                //extract the bluetooth device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //do cool stuff with the device
                String name = device.getName();
                String address = device.getAddress();
                //use rssi to get the device strength (nearer or further devices)
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                //Log.i("device found", "Name: " + name + " Address: " + address + "RSSI: " + rssi);
                //add the address if it isn't already in the list
                if(!addresses.contains(address)) {
                    addresses.add(address);
                    //use to check if the device has already been added to the list, we don't want repeats
                    String deviceString = "";
                    if (name == null || name.equals("")) {
                        //the name is null or an empty String so use the address instead
                        deviceString = address + " - RSSI" + rssi + "dBm";
                    } else {
                        deviceString = name + " - RSSI" + rssi + "dBm";
                    }

                    bluetoothDevices.add(deviceString);
                    //update the adapter to let it know there's new info to display
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
    };
}
