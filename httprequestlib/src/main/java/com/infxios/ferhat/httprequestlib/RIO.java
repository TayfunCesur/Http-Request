package com.infxios.ferhat.httprequestlib;

import org.json.JSONObject;

/**
 * Created by ferhat on 06.01.2016.
 */
public class RIO {
    public HttpResponseType result = HttpResponseType.HTTP_FORBIDDEN;
    public JSONObject response = null;
    public String response_str = null;
    public String exception = "";
    public String responseUrl = null;
    public Request request = null;

}
