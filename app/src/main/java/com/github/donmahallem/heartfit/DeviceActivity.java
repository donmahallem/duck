package com.github.donmahallem.heartfit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.donmahallem.heartfit.adapter.DeviceRecyclerViewAdapter;
import com.github.donmahallem.heartfit.viewholder.OnBleDeviceClaimListener;
import com.github.donmahallem.heartfit.viewmodel.DeviceActivityViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

public class DeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private DeviceRecyclerViewAdapter mDeviceAdapter;
    private final BleScanCallback mBleScanCallback = new BleScanCallback() {
        @Override
        public void onDeviceFound(BleDevice bleDevice) {
            Timber.d("Device Found: %s", bleDevice.getName());
            DeviceActivity.this
                    .mDeviceAdapter.addUnclaimedBleDevice(bleDevice);
        }

        @Override
        public void onScanStopped() {
            Timber.i("Device Scan stopped");
        }
    };
    private OnBleDeviceClaimListener mListener = new OnBleDeviceClaimListener() {
        @Override
        public void onClaimDevice(BleDevice bleDevice) {
            Timber.d("Device clicked");
            unclaimDevice(bleDevice);
        }
    };
    private DeviceActivityViewModel mViewModel;
    private Button mBtnBleScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        this.mRecyclerView = this.findViewById(R.id.recyclerView);
        this.mDeviceAdapter = new DeviceRecyclerViewAdapter();
        this.mDeviceAdapter.setOnClaimDeviceListener(this.mListener);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.mRecyclerView.setAdapter(this.mDeviceAdapter);
        this.mBtnBleScan = this.findViewById(R.id.btnStartScanForDevices);
        this.mBtnBleScan.setOnClickListener(this);
        this.mViewModel = ViewModelProviders.of(this).get(DeviceActivityViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.startScanForDevices();
        this.getAvailableDevices();
    }

    private void startScanForDevices() {
        final List<DataType> typeList = new ArrayList<>();
        typeList.add(DataType.TYPE_HEART_RATE_BPM);
        typeList.add(DataType.AGGREGATE_HEART_RATE_SUMMARY);
        Fitness.getBleClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .startBleScan(typeList, 100, this.mBleScanCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopScanForDevices();
    }

    private void stopScanForDevices() {
        Fitness.getBleClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .stopBleScan(this.mBleScanCallback)
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(Task<Boolean> task) {
                        Timber.d("Stopped scan: %s", task.getResult());
                    }
                });
    }

    public void getAvailableDevices() {
        Fitness.getBleClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .listClaimedBleDevices()
                .addOnSuccessListener(new OnSuccessListener<List<BleDevice>>() {
                    @Override
                    public void onSuccess(List<BleDevice> bleDevices) {
                        mDeviceAdapter.addClaimedBleDevices(bleDevices);
                    }
                });
    }

    public void claimDevice(BleDevice device) {

        Fitness.getBleClient(this,
                GoogleSignIn.getLastSignedInAccount(this))
                .claimBleDevice(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("Device claimed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Timber.e(e);
                    }
                });
    }

    public void unclaimDevice(BleDevice device) {
        Fitness.getBleClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .unclaimBleDevice(device)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("Device unclaimed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Timber.e(e);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStartScanForDevices:
                this.toggleScan();
                break;
        }
    }

    private void toggleScan() {
        if (this.mViewModel.isScanningForDevices().getValue()) {
            startScanForDevices();
        } else {
            stopScanForDevices();
        }
    }
}
