package com.bpt.tipi.streaming.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bpt.tipi.streaming.ItemVideoFragment;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.helper.VideoNameHelper;
import com.bpt.tipi.streaming.model.ItemVideo;
import com.bpt.tipi.streaming.model.Label;
import com.bpt.tipi.streaming.persistence.Database;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaggedActivity extends AppCompatActivity implements View.OnClickListener {

    ViewPager mViewPager;
    List<ItemVideo> files;
    Button btnTagged;
    Spinner spOptions;

    SectionsPagerAdapter adapter;
    TextView tvTitle;

    Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged);

        database = new Database(TaggedActivity.this);
        try {
            database.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mViewPager = findViewById(R.id.container);
        btnTagged = findViewById(R.id.btnTagged);
        spOptions = findViewById(R.id.spOptions);
        tvTitle = findViewById(R.id.textView_title);

        btnTagged.setOnClickListener(this);

        files = getFiles(VideoNameHelper.getMediaFolder());

        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                spOptions.setSelection(0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        loadSpinner();
    }

    public void loadSpinner() {
        List<Label> labels = database.getLabels();
        List<String> names = new ArrayList<>();
        ArrayAdapter<String> adapter = null;
        if (labels.size() > 0) {
            names.add("Seleccione...");
            for (int i = 0; i < labels.size(); i++) {
                names.add(labels.get(i).description);
                adapter = new ArrayAdapter<>(
                        TaggedActivity.this, android.R.layout.simple_spinner_item,
                        names);
            }
        } else {
            String[] defaultLabels = getResources().getStringArray(R.array.labels_tagging);
            adapter = new ArrayAdapter<>(
                    TaggedActivity.this, android.R.layout.simple_spinner_item,
                    defaultLabels);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spOptions.setAdapter(adapter);
        spOptions.setSelection(0);
    }

    private List<ItemVideo> getFiles(File f) {
        File[] dirs = f.listFiles();
        List<ItemVideo> files = new ArrayList<>();
        try {
            for (File file : dirs) {
                if (Utils.isVideoAndNotLabel(file)) {
                    files.add(new ItemVideo(file.getName(), file.getAbsolutePath(), "file_icon"));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(files);
        return files;
    }

    @Override
    public void onClick(View view) {
        if (spOptions.getSelectedItemPosition() != 0) {
            int id = Utils.getIdLabel(spOptions.getSelectedItemPosition());
            VideoNameHelper.taggedVideo(adapter.getPageTitle(mViewPager.getCurrentItem()).toString(), id);
            Toast.makeText(TaggedActivity.this, "Video etiquetado con éxito", Toast.LENGTH_SHORT).show();
            adapter.removeItem(mViewPager.getCurrentItem());
            spOptions.setSelection(0);
        } else {
            Toast.makeText(TaggedActivity.this, "Por favor seleccione una etiqueta", Toast.LENGTH_SHORT).show();
        }
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ItemVideoFragment.newInstance(files.get(position).path);
        }

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return files.get(position).name;
        }

        public void removeItem(int position) {
            files.remove(position);
            this.notifyDataSetChanged();
            if (getCount() == 0) {
                Toast.makeText(TaggedActivity.this, "No tiene mas videos pendientes por etiquetar", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
