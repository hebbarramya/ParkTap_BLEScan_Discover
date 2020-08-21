package com.parking.parktap.parktap_application;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parking.parktap.modal.LeDeviceListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;


public class ScanBLE extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private boolean mScanning;
    public boolean mconnected;
    private Handler mHandler;
    BluetoothLeScannerCompat scanner;

    private BluetoothGatt mGatt;
    ArrayList<BluetoothDevice> Btdevicelist = new ArrayList<>();

    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private static final ParcelUuid BASE_UUID = ParcelUuid.fromString("aaaa0000-1911-6b29-2a48-34cbffc4a7ad");
    //private  static  final  String SERVICE_UUID="80808000-0000-0000-1000-000000000000";
    private static final String TAG = "ScanBLE";
    private Context _context;
    private Button btnconnect;
    //private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_ble);

        //Initialize Handler
        mHandler = new Handler();

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        requestBluetoothEnable();
        checkLocationPermission();


    }

    @Override
    protected void onResume() {
        super.onResume();
        _context = ScanBLE.this;

        checkBLEsupport();
        addRecyclerView();
        scanLeDevice(true);
    }

    private void requestBluetoothEnable() {
        //Enable Bluetooth
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void checkBLEsupport() {

        //Check For the BLE Avaialability
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    private void addRecyclerView() {
        //Intialize the Recycler View
        RecyclerView recyclerView = findViewById(R.id.devicelist);
        //Adds Divider between rows
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        //Set the Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, Btdevicelist);
        recyclerView.setAdapter(mLeDeviceListAdapter);


        // row click listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null) return;

                final Intent intent = new Intent(ScanBLE.this, DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }



    //Scanning
    private void scanLeDevice(final boolean enable) {
        final BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner != null) {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothLeScanner.stopScan(scanCallback);
                        mScanning = false;

                    }
                }, SCAN_PERIOD);


//        //scan specified devices only with ScanFilter
                ScanFilter scanFilter =
                        new ScanFilter.Builder()
                                .setServiceUuid(BASE_UUID)
                                .build();
                List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
                scanFilters.add(scanFilter);

                ScanSettings scanSettings =
                        new ScanSettings.Builder().build();

                mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);

//
//
//
//
//
//       // mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
                // mBluetoothLeScanner.startScan(scanCallback);
                mScanning = true;


            } else {
                mBluetoothLeScanner.stopScan(scanCallback);
            }
            mScanning = false;

        }
    }

    //Scan Call Back
    private ScanCallback scanCallback = new ScanCallback() {


        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addBluetoothDevice(result.getDevice());
            Log.d(TAG,"onScanResult="+result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                addBluetoothDevice(result.getDevice());
            }
            Log.d(TAG,"BatchScanResult="+results.toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
//            Toast.makeText(MainActivity.this,
//                    "onScanFailed: " + String.valueOf(errorCode),
//                    Toast.LENGTH_LONG).show();
        }

        private void addBluetoothDevice(final BluetoothDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    mLeDeviceListAdapter.addDevice(device);

                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

    };


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                new AlertDialog.Builder(this)
                        .setTitle("Location Access")
                        .setMessage("Do you want to access location?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ScanBLE.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


}




