// Copyright 2013 Google Inc. All Rights Reserved.

package com.google.android.gms.drive.sample.texteditor;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.widget.DataBufferAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * List adapter for drive Metadata objects. Sets the text as the metadata's title and uses the
 * simple_list_item_1 layout.
 */
public class MetadataAdapter extends DataBufferAdapter<Metadata> {

    private static final int RESOURCE_ID = android.R.layout.simple_list_item_1;

    private final Context mContext;

    public MetadataAdapter(Context context) {
        super(context, RESOURCE_ID);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;

        if (convertView == null) {
            view = (TextView) ((Activity) mContext).getLayoutInflater().
                inflate(RESOURCE_ID, parent, false);
        } else {
            view = (TextView) convertView;
        }

        Metadata entry = getItem(position);
        view.setText(entry.getTitle());

        return view;
    }
}
