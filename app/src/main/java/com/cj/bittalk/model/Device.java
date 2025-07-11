package com.cj.bittalk.model;

import android.Manifest;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.RequiresPermission;

public class Device {
    private String name;
    private String address;
    private boolean isPaired;
    private BluetoothDevice bluetoothDevice;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public Device(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        this.name = bluetoothDevice.getName() != null ? bluetoothDevice.getName() : "Unknown Device";
        this.address = bluetoothDevice.getAddress();
        this.isPaired = bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    // Getters and setters
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

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
}