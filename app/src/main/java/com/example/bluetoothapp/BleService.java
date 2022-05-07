package com.example.bluetoothapp;

import java.util.ArrayList;
import java.util.HashMap;

public class BleService {
    private HashMap<String, String> serviceUuid;
    private HashMap<String, ArrayList<String>> characteristicUuids;

    public HashMap<String, String> getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(HashMap<String, String> serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public HashMap<String, ArrayList<String>> getCharacteristicUuids() {
        return characteristicUuids;
    }

    public void setCharacteristicUuids(HashMap<String, ArrayList<String>> characteristicUuids) {
        this.characteristicUuids = characteristicUuids;
    }

    @Override
    public String toString() {
        return "BleService{" +
                "serviceUuid=" + serviceUuid +
                ", characteristicUuids=" + characteristicUuids +
                '}';
    }
}
