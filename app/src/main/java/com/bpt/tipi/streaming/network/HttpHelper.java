/*
 * Copyright (c) 2016. BPT Integration S.A.S. - Todos los derechos reservados.
 * La copia no autorizada de este archivo, a través de cualquier medio está estrictamente prohibida.
 * Escrito por Jorge Pujol <jpujolji@gmail.com>, diciembre 2016.
 */

package com.bpt.tipi.streaming.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class HttpHelper {

    public static abstract class TypeRequest {
        public static final int TYPE_POST = 1;
        public static final int TYPE_GET = 2;
        public static final int TYPE_PUT = 3;
    }

    public static abstract class Method {
        public static final String REPORT_STATUS = "/devices/report-status";
        public static final String SOS = "/devices/start-sos";
        public static final String REGISTER_ID = "/devices/register";
        public static final String LOGIN = "/buscarUsuario";
        public static final String LABELS = "/findLabelsCamera";
        public static final String LOG_STREAMING = "/streaming-logs";
        public static final String LOGIN_SERVER = "/devices/login";
        public static final String SEND_CONFIG = "/device-configurations";
    }

    /**
     * @param context a utilizar para comprobar la conectividad.
     * @return true si esta conectado, false si no.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
