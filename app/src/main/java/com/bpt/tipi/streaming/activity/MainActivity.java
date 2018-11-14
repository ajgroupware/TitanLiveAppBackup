package com.bpt.tipi.streaming.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bpt.tipi.streaming.CustomEditText;
import com.bpt.tipi.streaming.R;
import com.bpt.tipi.streaming.ServiceHelper;
import com.bpt.tipi.streaming.UnCaughtException;
import com.bpt.tipi.streaming.Utils;
import com.bpt.tipi.streaming.helper.LaserHelper;
import com.bpt.tipi.streaming.helper.VideoNameHelper;
import com.bpt.tipi.streaming.model.Label;
import com.bpt.tipi.streaming.model.MessageEvent;
import com.bpt.tipi.streaming.network.HttpClient;
import com.bpt.tipi.streaming.network.HttpHelper;
import com.bpt.tipi.streaming.network.HttpInterface;
import com.bpt.tipi.streaming.persistence.Database;
import com.bpt.tipi.streaming.service.RecorderService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HttpInterface {

    //CameraServiceV2 cameraService;
    RecorderService cameraService;
    boolean mBound = false;
    private EventBus bus = EventBus.getDefault();
    TextView tvRecord, tvTimeElapsed;
    private Dialog tagDialog;

    //Varible para guardar el usuario que se ingresa en el dialog de login.
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(this));

        tvRecord = findViewById(R.id.tvRecord);
        tvTimeElapsed = findViewById(R.id.tvTimeElapsed);
        LinearLayout linearCamera = findViewById(R.id.linearCamera);
        RelativeLayout relativeTagging = findViewById(R.id.linearTagging);
        LinearLayout linearSettings = findViewById(R.id.linearSettings);
        LinearLayout linearLogin = findViewById(R.id.linearLogin);
        LinearLayout linearFlash = findViewById(R.id.linearLamp);
        LinearLayout linearMotoTalk = findViewById(R.id.linearMotoTalk);
        LinearLayout linearRecord = findViewById(R.id.linearRecord);
        LinearLayout linearLaser = findViewById(R.id.linearLaser);

        linearCamera.setOnClickListener(this);
        relativeTagging.setOnClickListener(this);
        linearSettings.setOnClickListener(this);
        linearLogin.setOnClickListener(this);
        linearFlash.setOnClickListener(this);
        linearMotoTalk.setOnClickListener(this);
        linearRecord.setOnClickListener(this);
        linearLaser.setOnClickListener(this);

        initServices();
        lockStatusBar();

    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.linearCamera:
                //intent = new Intent();
                //intent.setClassName("com.android.gallery3d", "com.android.camera.CameraActivity");
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
                break;
            case R.id.linearTagging:
                //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                //final boolean isLogin = preferences.getBoolean("isLogin", false);
                //if (isLogin) {
                int countPending = Utils.getNumberPending();
                if (countPending > 0) {
                    startActivity(new Intent(MainActivity.this, TaggedActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "No tiene videos pendientes por etiquetar", Toast.LENGTH_SHORT).show();
                }
                //} else {
                //    Toast.makeText(MainActivity.this, "Para poder etiquetar videos debe loguearse en el dispositivo", Toast.LENGTH_SHORT).show();
                //}
                break;
            case R.id.linearSettings:
                showDialogPassword();
                break;
            case R.id.linearLogin:
                showDialogLogin();
                break;
            case R.id.linearLamp:
                MessageEvent event = new MessageEvent(MessageEvent.STATE_FLASH);
                bus.post(event);
                break;
            case R.id.linearRecord:
                try {
                    intent = getPackageManager().getLaunchIntentForPackage("com.android.soundrecorder");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Opción no disponible en el dispositivo", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.linearMotoTalk:
                try {
                    intent = getPackageManager().getLaunchIntentForPackage("com.motorola.ptt");
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Opción no disponible en el dispositivo", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.linearLaser:
                LaserHelper.setLaserState();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);
        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent, conCameraService, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
        if (mBound) {
            unbindService(conCameraService);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setIdStatus();
        setPendingVideoForLabel();
        if (cameraService != null && cameraService.isLocalRecording) {
            tvRecord.setVisibility(View.VISIBLE);
            tvTimeElapsed.setVisibility(View.VISIBLE);
            if (tagDialog != null && tagDialog.isShowing()) {
                tagDialog.dismiss();
            }
        } else {
            tvRecord.setVisibility(View.INVISIBLE);
            tvTimeElapsed.setVisibility(View.INVISIBLE);
        }
        bindService();
        if (getLabels().size() == 0) {
            loadLabels();
        }
        setupTagDialog();
    }

    public void bindService() {
        try {
            Intent intent = new Intent(this, RecorderService.class);
            bindService(intent, conCameraService, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(final MessageEvent event) {
        switch (event.key) {
            case MessageEvent.START_LOCAL_RECORDING:
                if (tagDialog.isShowing()) {
                    tagDialog.dismiss();
                }
                tvRecord.setVisibility(View.VISIBLE);
                tvTimeElapsed.setVisibility(View.VISIBLE);
                break;
            case MessageEvent.STOP_LOCAL_RECORDING:
                if (getLabels().size() == 0) {
                    getLabels();
                } else {
                    if (!tagDialog.isShowing()) {
                        tagDialog.show();
                    }
                }
                tvRecord.setVisibility(View.INVISIBLE);
                tvTimeElapsed.setVisibility(View.INVISIBLE);
                break;
            case MessageEvent.TIME_ELAPSED:
                tvTimeElapsed.setText(event.content);
                break;
            case MessageEvent.FINISH_SERVICES:
                if (mBound) {
                    unbindService(conCameraService);
                    mBound = false;
                }
                break;
            case MessageEvent.START_SERVICES:
                bindService();
                break;
        }
    }

    public void setPendingVideoForLabel() {
        TextView tvVideoPending = findViewById(R.id.tvVideoPending);
        int countPending = Utils.getNumberPending();
        if (countPending > 99) {
            tvVideoPending.setText("" + 99);
        } else {
            tvVideoPending.setText("" + Utils.getNumberPending());
        }
    }

    /**
     * Bloque la barra de estado del dispositivo
     */
    public void lockStatusBar() {
        WindowManager manager = ((WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE));

        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;

        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (30 * getResources()
                .getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;

        CustomViewGroup view = new CustomViewGroup(this);

        if (manager != null) {
            manager.addView(view, localLayoutParams);
        }
    }

    public void initServices() {
        ServiceHelper.startCheckPeriodicalService(MainActivity.this);
        ServiceHelper.startRecorderService(MainActivity.this);
        ServiceHelper.startMqttService(MainActivity.this);
        ServiceHelper.startLocationService(MainActivity.this);
        loadLabels();
    }

    public void showDialogPassword() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String password = preferences.getString(getString(R.string.general_key_settings_password), getString(R.string.pref_default_settings_password));
        final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.enter_password_dialog);
        dialog.setCancelable(true);

        final EditText etPassword = dialog.findViewById(R.id.etPassword);
        Button btnAccept = dialog.findViewById(R.id.btnAccept);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etPassword.getText().toString().isEmpty()) {
                    if (etPassword.getText().toString().equals(password)) {
                        dialog.dismiss();
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        etPassword.setText("");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showDialogLogin() {
        final Dialog dialog = new Dialog(MainActivity.this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.login_dialog);
        dialog.setCancelable(true);

        final LinearLayout linearLogin = dialog.findViewById(R.id.linearLogin);
        final LinearLayout linearExit = dialog.findViewById(R.id.linearExit);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final boolean isLogin = preferences.getBoolean("isLogin", false);

        if (isLogin) {
            linearLogin.setVisibility(View.INVISIBLE);
            linearExit.setVisibility(View.VISIBLE);
        } else {
            linearLogin.setVisibility(View.VISIBLE);
            linearExit.setVisibility(View.INVISIBLE);
        }

        final CustomEditText etUser = dialog.findViewById(R.id.etUser);
        final CustomEditText etPassword = dialog.findViewById(R.id.etPassword);

        Button btnAccept = dialog.findViewById(R.id.btnAccept);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnExit = dialog.findViewById(R.id.btnExit);
        TextView tvUser = dialog.findViewById(R.id.tvUser);
        tvUser.setText(preferences.getString("user", ""));

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText();
                username = user;
                String password = etPassword.getText();
                if (!user.isEmpty() && !password.isEmpty()) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("usuario", user);
                        json.put("cotrasena", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HttpClient httpClient = new HttpClient(MainActivity.this);
                    httpClient.httpRequest(json.toString(), HttpHelper.Method.LOGIN, HttpHelper.TypeRequest.TYPE_POST, true);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isLogin", false);
                editor.putString("user", "");
                editor.apply();
                linearLogin.setVisibility(View.VISIBLE);
                linearExit.setVisibility(View.INVISIBLE);
                setIdStatus();
            }
        });
        dialog.show();
    }

    private void setupTagDialog() {
        tagDialog = new Dialog(MainActivity.this, R.style.CustomDialogTheme);
        tagDialog.setContentView(R.layout.tag_video_dialog);
        tagDialog.setTitle("Etiquetar video");
        tagDialog.setCancelable(true);

        final Spinner spOptions = tagDialog.findViewById(R.id.spOptions);
        Button btnAccept = tagDialog.findViewById(R.id.btnAccept);
        Button btnCancel = tagDialog.findViewById(R.id.btnCancel);

        final List<Label> labels = getLabels();

        if (labels.size() > 0) {
            List<String> names = new ArrayList<>();
            names.add("Seleccione...");
            for (int i = 0; i < labels.size(); i++) {
                names.add(labels.get(i).description);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    MainActivity.this, R.layout.item_spinner_tag,
                    names);

            adapter.setDropDownViewResource(R.layout.item_spinner_tag);

            spOptions.setAdapter(adapter);
            spOptions.setSelection(0);
        }

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spOptions.getSelectedItemPosition() != 0) {
                    Toast.makeText(MainActivity.this, "Video etiquetado con éxito", Toast.LENGTH_SHORT).show();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    String nameVideo = preferences.getString("name_video_local", "");
                    Label label = labels.get(spOptions.getSelectedItemPosition() - 1);
                    VideoNameHelper.taggedVideo(nameVideo, label.id);
                    tagDialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "Seleccione una etiqueta", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPendingVideoForLabel();
                tagDialog.dismiss();
            }
        });
    }

    public List<Label> getLabels() {
        Database database = new Database(MainActivity.this);
        try {
            database.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Label> labels = database.getLabels();
        List<String> ids = new ArrayList<>();
        if(labels.size()== 0){
            String[] defaultLabels = getResources().getStringArray(R.array.labels_tagging);
            String[] defaultLabelIds = getResources().getStringArray(R.array.ids_tagging);

            for(String id: defaultLabelIds){
                ids.add(id);
            }

            int id= 0;
            for (String label : defaultLabels) {
                Label lb = new Label(Integer.valueOf(ids.get(id)), label);
                labels.add(lb);
                id++;
            }
        }
        database.close();
        return labels;
    }

    public void setIdStatus() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String idDevice = preferences.getString(getString(R.string.general_key_id_device), "");
        String user = preferences.getString("user", "");

        TextView tvIdCamera = findViewById(R.id.tvIdCamera);
        if (idDevice.isEmpty()) {
            tvIdCamera.setText("IdCamera: No configurado");
        } else {
            tvIdCamera.setText("IdCamera: " + idDevice + " " + user);
        }
    }

    @Override
    public void onSuccess(String method, JSONObject response) {
        switch (method) {
            case HttpHelper.Method.LOGIN:
                if (response.optString("status", "").equals("OK") && response.optString("data", "").equals("true")) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (response.optString("data", "").equals("true")) {
                        JSONObject user = response.optJSONObject("user");
                        editor.putString("user", user.optString("name") + " " + user.optString("lastname"));
                    }
                    editor.putBoolean("isLogin", true);
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Login realizado con éxito", Toast.LENGTH_LONG).show();
                    setIdStatus();
                    JSONObject json = new JSONObject();
                    try {
                        json.put("deviceName", preferences.getString(getString(R.string.id_device), ""));
                        json.put("loggedUser", username);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    HttpClient httpClient = new HttpClient(MainActivity.this);
                    httpClient.httpRequest(json.toString(), HttpHelper.Method.LOGIN_SERVER, HttpHelper.TypeRequest.TYPE_POST, true);
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos, verifique sus credenciales e intente nuevamente", Toast.LENGTH_LONG).show();
                }

                break;
            case HttpHelper.Method.LOGIN_SERVER:

                break;
            case HttpHelper.Method.LABELS:
                JSONObject object = response.optJSONObject("result");
                if (object != null && object.optString("code", "").equals("100")) {
                    JSONArray jsonArray = response.optJSONArray("labelsList");
                    if (jsonArray != null) {
                        Database database = new Database(MainActivity.this);
                        try {
                            database.open();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<List<Label>>() {
                        }.getType();
                        List<Label> labels = gson.fromJson(jsonArray.toString(), collectionType);
                        database.insertLabels(labels);
                        database.close();
                    }
                }
                break;
        }
    }

    @Override
    public void onFailed(String method, JSONObject errorResponse) {
        if (method != HttpHelper.Method.LABELS) {
            Toast.makeText(MainActivity.this, "Ocurrió un error al procesar su solicitud, por favor intente mas tarde", Toast.LENGTH_LONG).show();
        }
    }

    public class CustomViewGroup extends ViewGroup {

        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }

    private ServiceConnection conCameraService = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            //CameraServiceV2.LocalBinder binder = (CameraServiceV2.LocalBinder) service;
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
            cameraService = binder.getService();
            mBound = true;

            if (cameraService.isLocalRecording) {
                tvRecord.setVisibility(View.VISIBLE);
            } else {
                tvRecord.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void loadLabels() {
        List<Label> labels = getLabels();
        if (labels.size() == 0) {
            JSONObject json = new JSONObject();
            try {
                json.put("type", "0");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            HttpClient httpClient = new HttpClient(MainActivity.this);
            httpClient.httpRequest(json.toString(), HttpHelper.Method.LABELS, HttpHelper.TypeRequest.TYPE_POST, true);
        }
    }
}
