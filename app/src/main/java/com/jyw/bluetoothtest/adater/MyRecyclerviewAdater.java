package com.jyw.bluetoothtest.adater;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jyw.bluetoothtest.R;

import java.util.List;

public class MyRecyclerviewAdater extends RecyclerView.Adapter<MyRecyclerviewAdater.MyViewHolder> {

    private final Context mContext;
    private List<BluetoothDevice> nearDevices;
    private MyBluetoothDeviceClickListener bluetoothDeviceClickListener;

    public MyRecyclerviewAdater(Context context) {
        super();
        this.mContext  = context;
    }

    public void setOnItemClicked(MyBluetoothDeviceClickListener bluetoothDeviceClickListener){
        this.bluetoothDeviceClickListener = bluetoothDeviceClickListener;
    }

    public interface MyBluetoothDeviceClickListener{
        void OnDeviceClicked(int position);
    }

   public void setData(List<BluetoothDevice> data){
        this.nearDevices = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_list_item,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        BluetoothDevice device = nearDevices.get(position);
        holder.tvBtName.setText(device.getName()!=null?device.getName():"未知");
        holder.tvBtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothDeviceClickListener.OnDeviceClicked(position);
            }
        });
        if(device.getBondState()==BluetoothDevice.BOND_BONDED){
            holder.tvStatus.setText("已配对");
        }else{
            holder.tvStatus.setText("");

        }
    }

    @Override
    public int getItemCount() {
        return nearDevices==null?0:nearDevices.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        private final TextView tvBtName;
        private final TextView tvStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBtName = itemView.findViewById(R.id.tv_bluetooth_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }


    }
}
