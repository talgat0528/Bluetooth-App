package com.example.bluetoothapp;

import java.io.Serializable;
import java.util.ArrayList;

public class Device implements Serializable {
    private String name;
    private String address;
    private ArrayList<BleService> bleServices;
    private String deviceType;

    public Device(String name, String address, String deviceType) {
        this.name = name;
        this.address = address;
        this.deviceType = deviceType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }


    public ArrayList<BleService> getServices() {
        return bleServices;
    }

    public void setServices(ArrayList<BleService> bleServices) {
        this.bleServices = bleServices;
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
