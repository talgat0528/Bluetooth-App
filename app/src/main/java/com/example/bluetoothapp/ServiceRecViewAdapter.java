package com.example.bluetoothapp;

import static com.example.bluetoothapp.DeviceActivity.DEVICE_ADDRESS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ServiceRecViewAdapter extends RecyclerView.Adapter<ServiceRecViewAdapter.ViewHolder> {
    private static final String TAG = "SERVICES";
    ArrayList<BleService> services = new ArrayList<>();
    private Context mContext;
    public ServiceRecViewAdapter(Context mContext) {
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_service, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ServiceRecViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");
        holder.txtName.setText(services.get(position).toString());

    }
    @Override
    public int getItemCount() {
        return services.size();
    }
    public void setServices(ArrayList<BleService> services) {
        this.services = services;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView parent;
        private TextView txtName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.service_parent);
            txtName = itemView.findViewById(R.id.txtService);
        }
    }
}
