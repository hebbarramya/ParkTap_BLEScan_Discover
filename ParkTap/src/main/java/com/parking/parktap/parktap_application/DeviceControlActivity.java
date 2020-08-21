package com.parking.parktap.parktap_application;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import android.widget.Toolbar;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends Activity {
    private final static String TAG = "DeviceControlActivity";

    private BluetoothLeService mBluetoothLeService;

    private ExpandableListView Gattserviceslist;
    private BluetoothGattCharacteristic mnotifyCharacteristic;

    private String mDeviceName;
    private TextView mDataField;
    private String mDeviceAddress;
    private Button btnsend;

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final String DEVICE_NAME = "NAME";
    private final String DEVICE_UUID = "UUID";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_control_activity);
        Button btnSend = (Button) findViewById(R.id.btnsend);
        TextView txtalignactionbar = new TextView(getApplicationContext());
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, // Width of TextView
                ActionBar.LayoutParams.WRAP_CONTENT);
        txtalignactionbar.setLayoutParams(lp);
        txtalignactionbar.setGravity(Gravity.CENTER);
        txtalignactionbar.setText("SteelHead");
        txtalignactionbar.setTextColor(Color.YELLOW);
//

        //Set Action bar
        getActionBar().show();
        getActionBar().setHomeAsUpIndicator(R.drawable.back_button);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        // getActionBar().setDisplayShowTitleEnabled(false);
        //getActionBar().setCustomView(R.layout.action_bar_align);
        getActionBar().setCustomView(txtalignactionbar);


        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //Initialization
        Gattserviceslist = (ExpandableListView) findViewById(R.id.services_list);
        Gattserviceslist.setOnChildClickListener(servicesListClickListner);
        TextView deviceaddress = (TextView) findViewById(R.id.device_address);
        deviceaddress.setText(mDeviceAddress);

        Intent serviceIntent = new Intent(this, BluetoothLeService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mBluetoothLeService.disconnect();
                Toast.makeText(getApplicationContext(), "Disconnected " + mDeviceName, Toast.LENGTH_SHORT).show();
                Intent displaydevcieintent = new Intent(DeviceControlActivity.this, ScanBLE.class);
                startActivity(displaydevcieintent);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            // Toast.makeText(getApplicationContext(),"ServiceConnection",Toast.LENGTH_SHORT).show();
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            Toast.makeText(getApplicationContext(), "Connect", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, mGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);

        }
    }

    public void onSendClicked(View sender) {
        Log.d(TAG, "Inside onSend Clicked");
        String password = "Stevens1911";
        byte[] authDatapackt = getAuthDataPacket(password);
        mBluetoothLeService.writeCharacteristic(authDatapackt);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter mGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private byte[] getAuthDataPacket(String password) {
        Log.d(TAG, "Inside getAuthdatapackt");
        byte[] dataPckt = new byte[20];

        int dataLength = password.getBytes().length;
        Log.d(TAG, "Datalength=" + dataLength);

        dataPckt[0] = (byte) 1;
        dataPckt[1] = (byte) ((dataLength >> 8) & 0XFF);
        dataPckt[2] = (byte) (dataLength & 0XFF);

        byte[] asciidata = password.getBytes(StandardCharsets.UTF_8);
        for (int i = 3; i < dataLength + 3; i++) {
            dataPckt[i] = asciidata[i - 3];
        }
        Log.d(TAG, "datapckt=" + Arrays.toString(dataPckt));

        return dataPckt;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String parktapServiceString = getResources().getString(R.string.parktap_service);
        String parktapCharateristicString = getResources().getString(R.string.parktap_characteristic);


        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        //Iterates through Gatt Services
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(DEVICE_NAME, GattAttributes.Gattvalues(uuid, parktapServiceString));
            currentServiceData.put(DEVICE_UUID, uuid);
            gattServiceData.add(currentServiceData);

            //Iterates through GATT  characteristics
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> characteristic =
                    new ArrayList<BluetoothGattCharacteristic>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                characteristic.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        DEVICE_NAME, GattAttributes.Gattvalues(uuid, parktapCharateristicString));
                currentCharaData.put(DEVICE_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(characteristic);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{DEVICE_NAME, DEVICE_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{DEVICE_NAME, DEVICE_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        Gattserviceslist.setAdapter(gattServiceAdapter);

    }

    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mnotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mnotifyCharacteristic, false);
                                mnotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                            Toast.makeText(getApplicationContext(), "Read Characteristic", Toast.LENGTH_SHORT).show();
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mnotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };

}
