package com.github.donmahallem.heartfit.adapter;

import android.view.ViewGroup;

import com.github.donmahallem.heartfit.viewholder.ClaimableBleDeviceViewHolder;
import com.github.donmahallem.heartfit.viewholder.InfoViewHolder;
import com.github.donmahallem.heartfit.viewholder.OnBleDeviceClaimListener;
import com.github.donmahallem.heartfit.viewholder.TitleViewHolder;
import com.google.android.gms.fitness.data.BleDevice;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class ListItem {
    final int type;
    final Object data;

    public ListItem(int type, Object data) {
        this.type = type;
        this.data = data;
    }
}

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static int TYPE_CLAIMED_DEVICE = 1;
    public final static int TYPE_UNCLAIMED_DEVICE = 2;
    public final static int TYPE_TITLE = 3;
    public final static int TYPE_INFO = 4;

    private List<BleDevice> mUnclaimedDevices = new ArrayList<>();
    private List<BleDevice> mClaimedDevices = new ArrayList<>();
    private OnBleDeviceClaimListener mClaimListener;
    private List<ListItem> mListItems = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_CLAIMED_DEVICE:
            case TYPE_UNCLAIMED_DEVICE:
                return new ClaimableBleDeviceViewHolder(parent);
            case TYPE_TITLE:
                return new TitleViewHolder(parent);
            case TYPE_INFO:
                return new InfoViewHolder(parent);
            default:
                throw new RuntimeException("Unknown type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (this.getItemViewType(position)) {
            case TYPE_CLAIMED_DEVICE:
                this.bindDeviceViewHolder((ClaimableBleDeviceViewHolder) holder, position, true);
                break;
            case TYPE_INFO:
                this.bindInfoViewHolder((InfoViewHolder) holder, position);
                break;
            case TYPE_TITLE:
                this.bindTitleViewHolder((TitleViewHolder) holder, position);
                break;
            case TYPE_UNCLAIMED_DEVICE:
                this.bindDeviceViewHolder((ClaimableBleDeviceViewHolder) holder, position, false);
                break;
        }
    }

    private void bindTitleViewHolder(TitleViewHolder holder, int position) {
        holder.setTitle((String) this.mListItems.get(position).data);
    }

    private void bindInfoViewHolder(InfoViewHolder holder, int position) {
        holder.setText((String) this.mListItems.get(position).data);
    }

    private void bindDeviceViewHolder(ClaimableBleDeviceViewHolder holder, int position, boolean claimed) {
        holder.setBleDevice((BleDevice) this.mListItems.get(position).data, claimed);
        holder.setClaimListener(this.mClaimListener);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
    }

    @Override
    public int getItemCount() {
        return this.mListItems.size();
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public int getItemViewType(int position) {
        return this.mListItems.get(position).type;
    }

    public void recalculate() {
        List<ListItem> item = new ArrayList<>();
        item.add(new ListItem(TYPE_TITLE, "Claimed"));
        if (this.mClaimedDevices.size() > 0) {
            for (BleDevice device : this.mClaimedDevices) {
                item.add(new ListItem(TYPE_CLAIMED_DEVICE, device));
            }
        } else {
            item.add(new ListItem(TYPE_INFO, "No devices claimed"));
        }
        item.add(new ListItem(TYPE_TITLE, "Unclaimed"));
        if (this.mUnclaimedDevices.size() > 0) {
            for (BleDevice device : this.mUnclaimedDevices) {
                item.add(new ListItem(TYPE_UNCLAIMED_DEVICE, device));
            }
        } else {
            item.add(new ListItem(TYPE_INFO, "No devices found"));
        }
        this.mListItems.clear();
        this.mListItems.addAll(item);
        this.notifyDataSetChanged();
    }

    public void addUnclaimedBleDevice(BleDevice bleDevice) {
        this.mUnclaimedDevices.add(bleDevice);
        this.recalculate();
    }

    public void addClaimedBleDevice(BleDevice bleDevice) {
        this.mClaimedDevices.add(bleDevice);
        this.recalculate();
    }

    public void addClaimedBleDevices(List<BleDevice> bleDevices) {
        this.mClaimedDevices.addAll(bleDevices);
        this.recalculate();
    }

    public void setOnClaimDeviceListener(OnBleDeviceClaimListener listener) {
        this.mClaimListener = listener;
    }
}
