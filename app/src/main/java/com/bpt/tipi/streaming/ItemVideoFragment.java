package com.bpt.tipi.streaming;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bpt.tipi.streaming.activity.TaggedActivity;
import com.bumptech.glide.Glide;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemVideoFragment extends Fragment {

    private static final String STRING_PATH = "string_path";
    String path;
    View rootView;

    public static ItemVideoFragment newInstance(String path) {
        ItemVideoFragment fragment = new ItemVideoFragment();
        Bundle args = new Bundle();
        args.putString(STRING_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    public ItemVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString(STRING_PATH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_item_video, container, false);
        ImageView ivImage = rootView.findViewById(R.id.ivImage);
        Glide.with(rootView.getContext()).load(Uri.fromFile(new File(path))).into(ivImage);

        File file = new File(path);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                intent.setDataAndType(Uri.parse(path), "video/mp4");
                startActivity(intent);
            }
        });

        if (file != null && file.exists()) {
            ((TaggedActivity) getActivity()).setTitle(file.getName());
        }

        return rootView;
    }
}
