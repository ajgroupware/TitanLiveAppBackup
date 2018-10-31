/*
 * Copyright (c) 2016. BPT Integration S.A.S. - Todos los derechos reservados.
 * La copia no autorizada de este archivo, a través de cualquier medio está estrictamente prohibida.
 * Escrito por Jorge Pujol <jpujolji@gmail.com>, diciembre 2016.
 */

package com.bpt.tipi.streaming.network;

import org.json.JSONObject;

/**
 * Interface utilizada para enviar los datos de las peticiones HTTP desde la clase {@link .HttpClient}
 * hasta la vista o activity que implementa.
 */
public interface HttpInterface {

    /**
     * Envía la respuesta de la petición HTTP de id_tipo_lote JSONObject a la vista.
     *
     * @param method   método al cual se le realizó la petición HTTP.
     * @param response resultado de la petición HTTP.
     */
    void onSuccess(String method, JSONObject response);

    /**
     * Envía el error ocurrido durante la petición HTTP a la vista.
     *
     * @param method        método al cual se le realizó la petición HTTP.
     * @param errorResponse error que devuelve la petición.
     */
    void onFailed(String method, JSONObject errorResponse);
}