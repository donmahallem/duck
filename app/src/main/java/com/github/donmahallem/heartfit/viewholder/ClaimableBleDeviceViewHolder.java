package com.github.donmahallem.heartfit.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.donmahallem.heartfit.R;
import com.google.android.gms.fitness.data.BleDevice;

import java.lang.ref.WeakReference;

import androidx.annotation.CallSuper;

public class ClaimableBleDeviceViewHolder extends BleDeviceViewHolder implements View.OnClickListener {
    private final Button mBtnClaim;
    private final TextView mTxtTitle;
    private final TextView mTxtSubtitle;

    private WeakReference<OnBleDeviceClaimListener> mClaimListener;
    private boolean mClaimed;

    public ClaimableBleDeviceViewHolder(ViewGroup parent) {
        super(parent, R.layout.vh_claimable_ble_device);
        this.mTxtTitle = this.itemView.findViewById(R.id.txtTitle);
        this.mBtnClaim = this.itemView.findViewById(R.id.btnClaim);
        this.mTxtSubtitle = this.itemView.findViewById(R.id.txtSubtitle);
        this.mBtnClaim.setOnClickListener(this);
    }

    @CallSuper
    @Override
    public void setBleDevice(BleDevice bleDevice) {
        super.setBleDevice(bleDevice);
        this.mTxtTitle.setText(bleDevice.getName());
        this.mTxtSubtitle.setText(bleDevice.getAddress());
    }

    public void setBleDevice(BleDevice bleDevice, boolean claimed) {
        this.setBleDevice(bleDevice);
        this.mClaimed = claimed;
        this.mBtnClaim.setText(claimed ? "Unclaim" : "Claim");
    }

    public void setClaimListener(OnBleDeviceClaimListener claimListener) {
        if (this.mClaimListener != null) {
            this.mClaimListener.clear();
        }
        if (claimListener == null) {
            this.mClaimListener = null;
        } else {
            mClaimListener = new WeakReference<>(claimListener);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnClaim) {
            if (this.mClaimListener != null && this.mClaimListener.get() != null) {
                this.mClaimListener.get().onClaimDevice(this.getBleDevice());
            }
        }
    }
}
