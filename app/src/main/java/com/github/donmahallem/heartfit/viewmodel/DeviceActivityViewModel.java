package com.github.donmahallem.heartfit.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DeviceActivityViewModel extends ViewModel {
    private MutableLiveData<Boolean> mIsScanningForDevices = new MutableLiveData<>();

    public DeviceActivityViewModel() {
        this.mIsScanningForDevices.setValue(false);
    }

    public LiveData<Boolean> isScanningForDevices() {
        return this.mIsScanningForDevices;
    }
}
