package com.bpt.tipi.streaming.model;

import android.support.annotation.NonNull;

public class ItemVideo implements Comparable<ItemVideo> {

    public String name, path, image;

    public ItemVideo(String mName, String mPath, String mImage) {
        name = mName;
        path = mPath;
        image = mImage;
    }

    @Override
    public int compareTo(@NonNull ItemVideo o) {
        return this.name.toLowerCase().compareTo(o.name.toLowerCase());
    }
}
