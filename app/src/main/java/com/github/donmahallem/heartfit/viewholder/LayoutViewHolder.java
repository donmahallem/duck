package com.github.donmahallem.heartfit.viewholder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class LayoutViewHolder extends RecyclerView.ViewHolder {
    public LayoutViewHolder(@NonNull ViewGroup group, @LayoutRes int layout) {
        super(LayoutInflater.from(group.getContext()).inflate(layout, group, false));
    }
}
