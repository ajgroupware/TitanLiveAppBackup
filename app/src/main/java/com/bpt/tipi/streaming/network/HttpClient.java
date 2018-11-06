/*
 * Copyright (c) 2016. BPT Integration S.A.S. - Todos los derechos reservados.
 * La copia no autorizada de este archivo, a través de cualquier medio está estrictamente prohibida.
 * Escrito por Jorge Pujol <jpujolji@gmail.com>, diciembre 2016.
 */

package com.bpt.tipi.streaming.network;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * La clase HttpClient es usada para realizar las peticiones HTTP en la aplicación Android, utilizando
 * la librería externa <a href="http://loopj.com/android-async-http">Android Asynchronous Http Client</a>.
 */
public class HttpClient {

    //Amazon
    public static final String URL = "http://52.24.232.162:8080/titan-live/api";
    public static final String URL_ALT = "http://52.24.232.162:8081/Evidencias-RestAPI/api/v1";
    //--

    //Amazon titanlive
    //public static final String URL = "http://54.218.193.119:8080/titan-live/api";
    //public static final String URL_ALT = "http://54.218.193.119:8081/Evidencias-RestAPI/api/v1";
    //--

    //movistar
    //public static final String URL = "http://10.80.63.235:8080/titan-live/api";
    //public static final String URL_ALT = "http://10.80.63.235:8081/Evidencias-RestAPI/api/v1";
    //--

    //Tigo y Avantel
    //public static final String URL = "http://10.50.4.1:8080/titan-live/api";
    //public static final String URL_ALT = "http://10.50.4.1:8081/Evidencias-RestAPI/api/v1";
    //--

    private HttpInterface mHttpInterface;
    private Context mContext;

    /**
     * Crea una nueva instancia del objeto HttpClient
     *
     * @param httpInterface interface para enviar los datos a la vista, vea {@link HttpInterface}.
     */
    public HttpClient(HttpInterface httpInterface) {
        mHttpInterface = httpInterface;
    }

    /**
     * Crea una nueva instancia del objeto HttpClient
     *
     * @param httpInterface interface para enviar los datos a la vista, vea {@link HttpInterface}.
     */
    public HttpClient(Context context, HttpInterface httpInterface) {
        mContext = context;
        mHttpInterface = httpInterface;
    }

    /**
     * Realiza una petición HTTP que puede ser síncrona o asíncrona, envía los datos de
     * respuesta a la vista por medio de un objeto de id_tipo_lote {@link HttpInterface}.
     *
     * @param jsonParams  parámetros para enviar con la petición HTTP.
     * @param method      método al cual se va a hacer la petición HTTP, definidos en {@link HttpHelper.Method}.
     * @param typeRequest válida el id_tipo_lote de petición HTTP que se va a realizar, ver {@link HttpHelper.TypeRequest}.
     */
    public void httpRequest(String jsonParams, final String method, int typeRequest, boolean isAsync) {
        String url;
        if (method.equals(HttpHelper.Method.LOGIN) || method.equals(HttpHelper.Method.LABELS)) {
            url = URL_ALT + method;
        } else {
            url = URL + method;
        }
        Log.i("Depuracion", "url request " + url);
        Log.i("Depuracion", "RequestParams " + jsonParams);
        AsyncHttpClient client;

        if (isAsync) {
            client = new AsyncHttpClient();
        } else {
            client = new SyncHttpClient();
        }

        client.setTimeout(20000);
        client.addHeader("Authorization", "Basic Y2FtZXJhOmNhbWVyNCoh");
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonParams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("Depuracion", "errorResponse " + errorResponse + " throwable " + throwable);
                mHttpInterface.onFailed(method, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("Depuracion", "response " + response);
                mHttpInterface.onSuccess(method, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Depuracion", "Error " + statusCode + " responseString " + responseString);
            }
        };

        switch (typeRequest) {
            case HttpHelper.TypeRequest.TYPE_POST:
                client.post(mContext, url, entity, "application/json", jsonHttpResponseHandler);
                break;
            case HttpHelper.TypeRequest.TYPE_GET:
                client.get(mContext, url, entity, "application/json", jsonHttpResponseHandler);
                break;
            case HttpHelper.TypeRequest.TYPE_PUT:
                client.put(mContext, url, entity, "application/json", jsonHttpResponseHandler);
                break;
        }
    }
}
