package com.github.donmahallem.heartfit.viewholder;

import com.google.android.gms.fitness.data.BleDevice;

public interface OnBleDeviceClaimListener {
    void onClaimDevice(BleDevice bleDevice);
}
