package com.jyw.bluetoothtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jyw.bluetoothtest.adater.MyRecyclerviewAdater;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "jyw";

    private static final int REQUEST_ENABLE_BT = 0x100;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 0x101;
    private Button btnClose;
    private Button btnOpen;
    private TextView tvState;
    private BluetoothAdapter btAdater;
    private Button btnSearchYpd;
    private View btnSearch;
    private MyBluetoothReceiver receiver;
    private MyBluetoothDeviceSearchReceiver searchReceiver;
    private List<BluetoothDevice> nearDevices;
    private RecyclerView recyclerView;


    private MyRecyclerviewAdater adater;
    private View btnDel12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nearDevices = new ArrayList<>();
        receiver = new MyBluetoothReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(BluetoothDevice.ACTION_FOUND);
        filter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        searchReceiver = new MyBluetoothDeviceSearchReceiver();
        registerReceiver(searchReceiver, filter1);


        initView();
        initBluetooth();
        LinearLayoutManager llManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llManager);
        adater = new MyRecyclerviewAdater(this);
        recyclerView.setAdapter(adater);
        adater.setOnItemClicked(new MyRecyclerviewAdater.MyBluetoothDeviceClickListener() {
            @Override
            public void OnDeviceClicked(int position) {
                Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                final BluetoothDevice device = nearDevices.get(position);
                Log.d(TAG, "OnDeviceClicked: 设备地址==" + device.getAddress() + device.getName());
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    boolean bond = device.createBond();

                }else{
                    Toast.makeText(MainActivity.this, "该设备已经被绑定", Toast.LENGTH_SHORT).show();

                   new Thread(){
                       @Override
                       public void run() {
                           super.run();

                           final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
                           UUID uuid = UUID.fromString(SPP_UUID);
                           try {
                               BluetoothSocket socket;
                               socket = device.createInsecureRfcommSocketToServiceRecord(uuid);

                               socket.connect();

                               Log.d(TAG, "run: 蓝牙socket已经连接");
                               OutputStream outputStream = socket.getOutputStream();
                               List<Byte> contents = new ArrayList<>();
                               String ceshiText = "测试";
                               byte[] ceshi = ceshiText.getBytes("GBk");
                               for (byte b:ceshi){
                                   contents.add(b);
                               }

                               StringBuilder builder = new StringBuilder();
                               int num = 5;
                               builder.delete(0,builder.length());
                               for(int i=0;i<num;i++){

                                   builder.append("\n");
                               }
                               byte[] kongge = builder.toString().getBytes("GBK");
                               for(byte b:kongge){
                                   contents.add(b);
                               }


                               byte[] data = new byte[contents.size()];
                               for(int i=0;i<contents.size();i++){
                                   data[i] = contents.get(i);
                               }





                               outputStream.write(data);

                               outputStream.flush();
                               outputStream.close();


                           } catch (IOException e) {
                               e.printStackTrace();
                           }

                       }
                   }.start();


                }


            }
        });


    }

    private void initBluetooth() {
        //初始化蓝牙
        btAdater = BluetoothAdapter.getDefaultAdapter();
        if (btAdater == null) {
            Toast.makeText(this, "该设备不支持蓝牙....", Toast.LENGTH_SHORT).show();
            return;
        }


    }




    private void initView() {
        btnClose = findViewById(R.id.btn_close);
        btnOpen = findViewById(R.id.btn_on);
        btnSearchYpd = findViewById(R.id.btn_search_ypd);
        btnSearch = findViewById(R.id.btn_search);
        btnDel12 = findViewById(R.id.btn_del12);
        tvState = findViewById(R.id.tv_bluetooth_state);
        recyclerView = findViewById(R.id.recyclerview);

        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnSearchYpd.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnDel12.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_on:
                if (btAdater != null && !btAdater.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                    tvState.setText("蓝牙已经开启");
                }
                break;
            case R.id.btn_close:
                if (btAdater != null && btAdater.isEnabled()) {
                    btAdater.disable();
                }
                break;
            case R.id.btn_search_ypd:
                if (btAdater != null && btAdater.isEnabled()) {
                    Set<BluetoothDevice> bondedDevices = btAdater.getBondedDevices();
                    if (bondedDevices.size() > 0) {
                        for (BluetoothDevice device : bondedDevices) {
                            String name = device.getName();
                            String address = device.getAddress();
                            Log.d(TAG, "onClick: 已配对的设备" + String.format("设备名：%1s 设备mac：%2s", name == null ? "未知" : name, address));
                        }
                    }
                }
                break;
            case R.id.btn_search:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                    } else {
                        startDiscovery();
                    }
                }


                break;
            case R.id.btn_del12:

                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                }
                break;
        }
    }

    private void startDiscovery() {
        //搜索设备
        if (btAdater != null && btAdater.isEnabled()) {
            nearDevices.clear();
            adater.setData(nearDevices);
            btAdater.startDiscovery();
            Toast.makeText(this, "开始搜索蓝牙设备...", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ENABLE_BT:
                    //请求开启蓝牙后返回
                    Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                    tvState.setText("蓝牙已开启");
                    break;
            }
        }
    }


    class MyBluetoothDeviceSearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    //如果是设备搜索动作
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String name = device.getName();
                    String address = device.getAddress();
                    Log.d(TAG, "onClick: 扫描到附近的设备" + String.format("设备名：%1s 设备mac：%2s", name, address));

                    nearDevices.add(device);
                    adater.setData(nearDevices);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice bondDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int bondState = bondDevice.getBondState();
                    if(bondState==BluetoothDevice.BOND_BONDING){
                        Log.d(TAG, "onReceive: 正在配对");
                    }else if(bondState==BluetoothDevice.BOND_BONDED){
                        Log.d(TAG, "onReceive: 已经绑定");
                    }else if(bondState == BluetoothDevice.BOND_NONE){
                        Log.d(TAG, "onReceive: 取消配对");
                    }
                    break;
            }
        }
    }


    class MyBluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            int previousExtra = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, 0);
            Log.d(TAG, "onReceive: == " + extra + " 以前的状态 ==" + previousExtra);

            switch (extra) {
                case BluetoothAdapter.STATE_ON:
                    Toast.makeText(context, "蓝牙已经开启状态", Toast.LENGTH_SHORT).show();
                    tvState.setText("蓝牙已经开启状态");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    Toast.makeText(context, "蓝牙已经关闭", Toast.LENGTH_SHORT).show();
                    tvState.setText("蓝牙已经关闭");
                    break;
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(searchReceiver);
    }
}
