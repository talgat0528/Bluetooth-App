package com.example.bluetoothapp;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

public class Device implements Serializable {
    private String name;
    private String address;
    private BluetoothDevice btdevice;

    public BluetoothDevice getBtdevice() {
        return btdevice;
    }

    public void setBtdevice(BluetoothDevice btdevice) {
        this.btdevice = btdevice;
    }

    public Device(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return  "name= " + name + '\n' +
                "address= " + address;
    }
}
