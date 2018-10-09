package com.github.donmahallem.heartfit.viewholder;

import android.view.ViewGroup;

import com.google.android.gms.fitness.data.BleDevice;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

public abstract class BleDeviceViewHolder extends LayoutViewHolder {
    private BleDevice mBleDevice;

    public BleDeviceViewHolder(@NonNull ViewGroup parent, @LayoutRes int layout) {
        super(parent, layout);
    }

    public BleDevice getBleDevice() {
        return mBleDevice;
    }

    @CallSuper
    public void setBleDevice(BleDevice bleDevice) {
        this.mBleDevice = bleDevice;
    }
}
