package com.example.bluetoothapp;

import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;
import static com.example.bluetoothapp.DeviceActivity.DEVICE_NAME;
import static com.example.bluetoothapp.DeviceActivity.DEVICE_OBJECT;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

public class DeviceRecViewAdapter extends RecyclerView.Adapter<DeviceRecViewAdapter.ViewHolder> {
    private static final String TAG = "DeviceRecViewAdapter";

    private ArrayList<Device> devices = new ArrayList<>();
    private Context mContext;

    public DeviceRecViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");
        holder.txtName.setText(devices.get(position).toString());
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,DeviceActivity.class);
                intent.putExtra(DEVICE_ADDRESS, devices.get(holder.getAdapterPosition()).getAddress());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }
    public void setOneDevice(Device item) {
        boolean containsItem = false;
        for(Device dev : this.devices) {
            if(dev.getName().equals(item.getName())) {
                containsItem = true;
                break;
            }
        }
        if(!containsItem) {
            this.devices.add(item);
            notifyDataSetChanged();
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView parent;
        private TextView txtName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtName = itemView.findViewById(R.id.txtDeviceName);
        }
    }
}
