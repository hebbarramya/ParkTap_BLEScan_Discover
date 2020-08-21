package com.parking.parktap.parktap_application;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.parking.parktap.parktap_application.GattAttributes.CHARACTERISTIC_READ_UUID;

//import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;

public class BluetoothLeService extends Service {
    private final static String TAG = "BluetootLeServcie";


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;


    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String   ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "GattCall Back");
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                
            } else {
                Log.w(TAG, "onServicesDiscoveonConnectionStateChangered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "Inside onCharacteristicRead");
            //readCounterCharacteristic(characteristic);


            Toast.makeText(getApplicationContext(),"Read OnCharacteristicsssssss",Toast.LENGTH_SHORT).show();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Toast.makeText(getApplicationContext(),"Read Success",Toast.LENGTH_SHORT).show();
                broadcastUpdate(ACTION_DATA_AVAILABLE);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Read UnSuccess",Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG,"Inside onDescriptorWrite");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "Inside OnCharacteristicWrite");
            if (status == gatt.GATT_SUCCESS) {
                Log.d(TAG, "Writing Successfull");
            } else {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
             super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "Inside OnCharacteristicChanged");
            readCounterCharacteristic(characteristic);
//            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
            }
    };

    private void readCounterCharacteristic(BluetoothGattCharacteristic
                                                   characteristic) {
        if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())) {
            byte[] data = characteristic.getValue();
            Log.d(TAG,"Response Data="+Arrays.toString(data));
            //int value = Ints.fromByteArray(data);
            // Update UI
        }
    }

    private void broadcastUpdate(String intentAction) {
        final Intent intent = new Intent(intentAction);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    //Connection
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        //Toast.makeText(getApplicationContext(),"ConnectGatt",Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;

    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Inside ReadCharacteristic");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        boolean status;
        status = mBluetoothGatt.readCharacteristic(characteristic);
        if (status) {
            Toast.makeText(getApplicationContext(), "Read success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Read unsuccess", Toast.LENGTH_SHORT).show();

        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }
//        public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
//            if(mBluetoothAdapter == null || mBluetoothGatt ==null){
//                Log.w(TAG,"BluetoothAdapter Not Initialized");
//                return;
//
//            }
//            Toast.makeText(getApplicationContext(),"Write Test",Toast.LENGTH_SHORT).show();
//            String textdata="Hello Mindteckers";
//            byte[] data = hexStringToByteArray(textdata);
//
//            characteristic.setValue(data);
//
//            boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
//
//        }

    //
//        private byte[] hexStringToByteArray(String s) {
//            int len = s.length();
//            byte[] data = new byte[len / 2];
//            for (int i = 0; i < len; i += 2) {
//                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
//                        .digit(s.charAt(i + 1), 16));
//            }
//            return data;
//        }
    public void writeCharacteristic(byte[] value) {
        Log.d(TAG, "writeCharacteristic");
        Toast.makeText(getApplicationContext(), "Writing", Toast.LENGTH_SHORT).show();
        // Toast.makeText(getApplicationContext(),"BluetoothGatt="+mBluetoothGatt,Toast.LENGTH_SHORT).show();
        BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(GattAttributes.PARKTAP_SERVICE_ID));
        Toast.makeText(getApplicationContext(), "Service=" + service, Toast.LENGTH_SHORT).show();

        if (service == null) {
            System.out.println("service null");
            return;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(GattAttributes.PARKTAP_CHARACTERISTIC_ID));
        Log.d(TAG, "characteristic=" + characteristic);
        System.out.println("characteristic" + characteristic.toString());

        if (characteristic == null) {
            System.out.println("characteristic null");
            return;
        }
        // Write Data
        System.out.println("Byte Value=" + Arrays.toString(value));
        characteristic.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
        System.out.println("Write Status: " + status);
        if (status) {
            Toast.makeText(getApplicationContext(), "Write success", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Write UnSucess", Toast.LENGTH_SHORT).show();

        }
    }


    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        Log.i(TAG, "Disconnected from GATT Server");
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        // Toast.makeText(getApplicationContext(),"List of Services",Toast.LENGTH_SHORT).show();
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


}
