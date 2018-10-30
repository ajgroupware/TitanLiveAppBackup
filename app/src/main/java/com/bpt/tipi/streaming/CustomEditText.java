/*
 * Copyright (c) 2018. BPT Integration S.A.S. - Todos los derechos reservados.
 * La copia no autorizada de este archivo, a través de cualquier medio está estrictamente prohibida.
 * Escrito por Jorge Pujol <jpujolji@gmail.com>, enero 2018.
 */

package com.bpt.tipi.streaming;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Control EditText personalizado.
 */

public class CustomEditText extends RelativeLayout implements View.OnFocusChangeListener {

    TextView tvTitle;
    String title;
    Drawable icon;
    int inputType;
    int maxLength;
    EditText etValue;
    TextInputLayout tiValue;
    private OnFocusChangeListener mListener;
    int mId;

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CustomEditText, 0, 0);

        try {
            title = a.getString(R.styleable.CustomEditText_title);
            inputType = a.getInt(R.styleable.CustomEditText_android_inputType, EditorInfo.TYPE_NULL);
            maxLength = a.getInt(R.styleable.CustomEditText_android_maxLength, 0);
        } finally {
            a.recycle();
        }
        LayoutInflater.from(context).inflate(R.layout.custom_edittext, this);

        tvTitle = findViewById(R.id.tvTitle);
        etValue = findViewById(R.id.etValue);
        tiValue = findViewById(R.id.tiValue);

        tiValue.setHint(title);

        if (inputType != EditorInfo.TYPE_NULL) {
            etValue.setInputType(inputType);
        }
        if (maxLength != 0) {
            etValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
        tvTitle.setText(title);

        etValue.setOnFocusChangeListener(this);

    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.d("Depuracion", "onFocusChange ");
        if (hasFocus) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setVisibility(View.VISIBLE);
                }
            }, 100);
        } else {
            if (etValue.getText().length() > 0) {
                tvTitle.setVisibility(View.VISIBLE);
            } else {
                tvTitle.setVisibility(View.INVISIBLE);
            }
        }
        if (mListener != null) {
            mListener.onFocusChange(mId, hasFocus);
        }
    }

    public String getText() {
        return etValue.getText().toString();
    }

    public void setText(String text) {
        etValue.setText(text);
    }

    public interface OnFocusChangeListener {
        void onFocusChange(int id, boolean hasFocus);
    }

    public void setFocusChangeListener(OnFocusChangeListener listener, int id) {
        mListener = listener;
        mId = id;
    }

}
