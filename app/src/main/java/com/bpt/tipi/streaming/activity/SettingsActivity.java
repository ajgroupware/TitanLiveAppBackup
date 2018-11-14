package com.bpt.tipi.streaming.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.bpt.tipi.streaming.ConfigHelper;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.UnCaughtException;
import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.model.RemoteConfig;
import com.bpt.tipi.streaming.mqtt.MqttService;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.bpt.tipi.streaming.service.LocationService;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.spec.ECField;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                preference.setSummary(stringValue.trim());
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof SwitchPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || VideoLocalPreferenceFragment.class.getName().equals(fragmentName)
                || ConnectionPreferenceFragment.class.getName().equals(fragmentName)
                || StreamingPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.header_general_settings) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    /**
     * Fragmento de las preferencias de la conexión al servidor
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, HttpInterface {

        String oldIdDevice = "";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            oldIdDevice = preferences.getString(getString(R.string.general_key_id_device), "");

            bindPreferenceSummaryToValue(findPreference(getString(R.string.general_key_id_device)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.general_key_settings_password)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.general_key_interval_location)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.general_key_interval_location_sos)));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.general_key_id_device))) {
                registerDevice();
            }
            if (key.equals(getString(R.string.general_key_interval_location))) {
                onIntervalConfigured();
            }
            RemoteConfig remoteConfig = ConfigHelper.getConfig(getActivity());
            Gson gson = new Gson();
            String json = gson.toJson(remoteConfig);
            HttpClient httpClient = new HttpClient(this);
            httpClient.httpRequest(json, HttpHelper.Method.SEND_CONFIG, HttpHelper.TypeRequest.TYPE_PUT, true);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        void onIdDeviceConfigured() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String idDevice = preferences.getString(getString(R.string.general_key_id_device), "");
            if (!idDevice.isEmpty()) {
                if (!Utils.isServiceRunning(getActivity(), MqttService.class)) {
                    getActivity().startService(new Intent(getActivity(), MqttService.class));
                } else {
                    EventBus bus = EventBus.getDefault();
                    MessageEvent messageEvent = new MessageEvent(MessageEvent.ID_DEVICE_CONFIGURED);
                    bus.post(messageEvent);
                }
                ServiceHelper.startLocationService(getActivity());
            } else {
                ServiceHelper.stopLocationService(getActivity());
                ServiceHelper.stopMqttService(getActivity());
            }
        }

        void onIntervalConfigured() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String idDevice = preferences.getString(getString(R.string.general_key_id_device), "");

            if (!idDevice.isEmpty()) {
                if (!Utils.isServiceRunning(getActivity(), LocationService.class)) {
                    getActivity().startService(new Intent(getActivity(), LocationService.class));
                } else {
                    ServiceHelper.stopLocationService(getActivity());
                    ServiceHelper.startLocationService(getActivity());
                }
            }
        }

        public void registerDevice() {
            JSONObject jsonObject = new JSONObject();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            try {
                jsonObject.put("imei", Utils.getImeiDevice(getActivity()));
                jsonObject.put("deviceName", preferences.getString(getString(R.string.general_key_id_device), ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpClient httpClient = new HttpClient(this);
            httpClient.httpRequest(jsonObject.toString(), HttpHelper.Method.REGISTER_ID, HttpHelper.TypeRequest.TYPE_POST, true);
        }

        @Override
        public void onSuccess(String method, JSONObject response) {
            if (response.optString("status").equals("1")) {
                try {
                    Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }

                onIdDeviceConfigured();
            } else {
                Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.general_key_id_device), oldIdDevice);
                editor.apply();
            }
        }

        @Override
        public void onFailed(String method, JSONObject errorResponse) {
            try {
                Toast.makeText(getActivity(), "Ocurrió un error al registrar el ID, intente mas tarde", Toast.LENGTH_LONG).show();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.general_key_id_device), oldIdDevice);
                editor.apply();
            } catch (Exception e) {

            }
        }
    }

    /**
     * Fragmento de las preferencias de la conexión al servidor
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConnectionPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, HttpInterface {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_host_address)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_port_number)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_stream_app_name)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_username)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_password)));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            RemoteConfig remoteConfig = ConfigHelper.getConfig(getActivity());
            Gson gson = new Gson();
            String json = gson.toJson(remoteConfig);
            HttpClient httpClient = new HttpClient(this);
            httpClient.httpRequest(json, HttpHelper.Method.SEND_CONFIG, HttpHelper.TypeRequest.TYPE_PUT, true);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSuccess(String method, JSONObject response) {
            if (response.optString("status").equals("1")) {
                Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailed(String method, JSONObject errorResponse) {

        }
    }

    /**
     * Fragmento de las preferencias de video grabados localmente
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VideoLocalPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, HttpInterface {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_video_local);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_video_size)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_framerate)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_video_duration)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_vibrate_and_sound)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_post_recorder)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.local_key_post_video_duration)));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            RemoteConfig remoteConfig = ConfigHelper.getConfig(getActivity());
            Gson gson = new Gson();
            String json = gson.toJson(remoteConfig);
            HttpClient httpClient = new HttpClient(this);
            httpClient.httpRequest(json, HttpHelper.Method.SEND_CONFIG, HttpHelper.TypeRequest.TYPE_PUT, true);
        }

        @Override
        public void onSuccess(String method, JSONObject response) {
            if (response.optString("status").equals("1")) {
                Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailed(String method, JSONObject errorResponse) {

        }
    }

    /**
     * Fragmento de las preferencias de video en streaming
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class StreamingPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, HttpInterface {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_video_streaming);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.streaming_key_video_size)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.streaming_key_framerate)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.streaming_key_vibrate_and_sound)));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            /*
            RemoteConfig remoteConfig = ConfigHelper.getConfig(getActivity());
            Gson gson = new Gson();
            String json = gson.toJson(remoteConfig);
            HttpClient httpClient = new HttpClient(this);
            httpClient.httpRequest(json, HttpHelper.Method.SEND_CONFIG, HttpHelper.TypeRequest.TYPE_PUT, true);
            */
        }

        @Override
        public void onSuccess(String method, JSONObject response) {
            if (response.optString("status").equals("1")) {
                try {
                    Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_LONG).show();
                } catch (Exception ignored) {

                }
            }
        }

        @Override
        public void onFailed(String method, JSONObject errorResponse) {

        }
    }
}