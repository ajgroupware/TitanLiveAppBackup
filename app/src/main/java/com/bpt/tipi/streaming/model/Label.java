package com.bpt.tipi.streaming.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jpujolji on 24/01/18.
 */

public class Label {
    @SerializedName("id")
    public int id;

    @SerializedName("nombre")
    public String description;

    public Label(int mId, String mDescription) {
        id = mId;
        description = mDescription;
    }
}
