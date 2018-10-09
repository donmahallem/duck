package com.github.donmahallem.heartfit.viewholder;

import android.view.ViewGroup;
import android.widget.TextView;

import com.github.donmahallem.heartfit.R;

/**
 * Created on 09.10.2018.
 */
public class TitleViewHolder extends LayoutViewHolder {
    private TextView mTxtTitle;

    public TitleViewHolder(ViewGroup parent) {
        super(parent, R.layout.vh_title);
        this.mTxtTitle = this.itemView.findViewById(R.id.txtTitle);
    }

    public void setTitle(String claimed) {
        this.mTxtTitle.setText(claimed);
    }

}
