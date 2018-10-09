package com.github.donmahallem.heartfit.viewholder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.github.donmahallem.heartfit.R;

/**
 * Created on 09.10.2018.
 */
public class InfoViewHolder extends LayoutViewHolder {
    private TextView mTxtTitle;

    public InfoViewHolder(ViewGroup parent) {
        super(parent, R.layout.vh_info);
        this.mTxtTitle = this.itemView.findViewById(R.id.txtTitle);
    }

    public void setText(String text) {
        this.mTxtTitle.setText(text);
    }
}
