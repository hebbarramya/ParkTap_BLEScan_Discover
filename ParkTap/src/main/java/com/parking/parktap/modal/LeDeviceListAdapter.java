package com.parking.parktap.modal;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parking.parktap.parktap_application.R;
import com.parking.parktap.parktap_application.ScanBLE;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.MyViewHolder> {

    private ArrayList<BluetoothDevice> mlistdevices;
    private LayoutInflater mInflater;

    public ScanBLE scanBLE = new ScanBLE();//Object of ScanBLE Class

    public BluetoothDevice device;
    private Context _context;


    public LeDeviceListAdapter(ScanBLE context, ArrayList<BluetoothDevice> devcieinfo) {
        super();
        mlistdevices = new ArrayList<BluetoothDevice>();
        this.mInflater = LayoutInflater.from(context);
        this.mlistdevices = devcieinfo;


    }

    @NonNull
    @Override
    public LeDeviceListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = mInflater.inflate(R.layout.device_list, viewGroup, false);
        return new MyViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull LeDeviceListAdapter.MyViewHolder myViewHolder, final int position) {

        final BluetoothDevice device = mlistdevices.get(position);
        myViewHolder.devcie_name.setText(device.getName());
        myViewHolder.device_id.setText(device.toString());
        Log.d(TAG,"deviceName" +device.getName());
//        myViewHolder.btnconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            //    String deviceinfo=mlistdevices
//
//                Toast.makeText(view.getContext(),"Hello"+String.valueOf(device.getAddress()),Toast.LENGTH_SHORT).show();
//
//            }
//        });

    }

        public void addDevice(BluetoothDevice device) {
            if (!mlistdevices.contains(device)) {
                mlistdevices.add(device);
            }
        }

    public BluetoothDevice getDevice(int position) {
        return mlistdevices.get(position);
    }

    public void clear() {
        mlistdevices.clear();
    }

    @Override
    public int getItemCount() {
        return mlistdevices.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView devcie_name;
        private TextView device_id;
        private Button btnconnect;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            device_id = (TextView) itemView.findViewById(R.id.deviceid);
            devcie_name = (TextView) itemView.findViewById(R.id.devicename);
            // btnconnect=(Button)itemView.findViewById(R.id.btnconnect);

            //  btnconnect.setOnClickListener(this);


        }


        @Override
        public void onClick(View view) {
            //Toast.makeText(view.getContext(),"position",String.valueOf(getAdapterPosition())),Toast.LENGTH_SHORT).show();
        }
    }
}



