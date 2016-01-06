package com.infxios.ferhat.httprequestlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ferhat on 05.01.2016.
 */
public abstract class HttpRequestFactory {

    private static SSLSocketFactory _defaultSocketFactory = null;
    private static HostnameVerifier _defaultHostnameVerifier = null;
    public static void TrustAllCertificates(boolean doYouTrust){

        if(!doYouTrust){
            if(_defaultHostnameVerifier != null)
                HttpsURLConnection.setDefaultHostnameVerifier(_defaultHostnameVerifier);

            if(_defaultSocketFactory != null)
                HttpsURLConnection.setDefaultSSLSocketFactory(_defaultSocketFactory);

            return;
        }

        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        HostnameVerifier trustAllHostNames = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        try{
            Properties properties = new Properties();
            properties.setProperty("jsse.enableSNIExtension","false");
            System.setProperties(properties);

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCertificates, new SecureRandom());

            if(_defaultSocketFactory == null)
                _defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

            if(_defaultHostnameVerifier == null)
                _defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostNames);

        }
        catch (GeneralSecurityException e){
            throw new ExceptionInInitializerError(e);
        }

    }
    private String _charSet = "UTF-8";
    protected Context context = null;

    private static HttpResponseType getResult(int result){
        return HttpResponseType.HTTP_OK;
    }

    private final class PostRequest extends Request{


        public PostRequest(String url){
            _url = url;
            requestType = RequestType.POST;
        }

        @TargetApi(Build.VERSION_CODES.CUPCAKE)
        @Override
        public void execute() {

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    _isBusy = true;
                    RIO rio = null;
                    try {
                        URL url = new URL(_url);

                        InputStream istream = null;

                        if(_isSecure){

                            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();

                            if(_timeout_on){
                                connection.setConnectTimeout(timeOut);
                            }

                            for(int i = 0; i<_requestHeaders.size(); i++){
                                String key = _requestHeaders.keys().nextElement();
                                String value = _requestHeaders.get(key);

                                connection.setRequestProperty(key,value);
                            }


                            connection.setDoOutput(true);

                            connection.connect();

                            if(_body != null){
                                OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                                OutputStreamWriter opwriter = new OutputStreamWriter(outputStream, _requestFactory.getCharset());
                                opwriter.write(_body);
                            }

                            rio = new RIO();
                            rio.result = HttpRequestFactory.getResult(connection.getResponseCode());
                            rio.responseUrl = _url;
                            rio.request = PostRequest.this;
                            istream = new BufferedInputStream(connection.getInputStream());
                        }
                        else{
                            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                            if(_timeout_on){
                                connection.setConnectTimeout(timeOut);
                            }

                            for(int i = 0; i<_requestHeaders.size(); i++){
                                String key = _requestHeaders.keys().nextElement();
                                String value = _requestHeaders.get(key);

                                connection.setRequestProperty(key,value);
                            }

                            connection.setDoOutput(true);

                            connection.connect();

                            if (_body != null){
                                OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                                OutputStreamWriter opwriter = new OutputStreamWriter(outputStream,_requestFactory.getCharset());
                                opwriter.write(_body);
                            }

                            rio = new RIO();
                            rio.result = HttpRequestFactory.getResult(connection.getResponseCode());
                            rio.responseUrl = _url;

                            istream = new BufferedInputStream(connection.getInputStream());

                        }

                        BufferedReader breader = new BufferedReader(new InputStreamReader(istream,_requestFactory.getCharset()));

                        String tmpString = null;

                        StringBuilder sbuilder = new StringBuilder();

                        while((tmpString = breader.readLine()) != null){
                            sbuilder.append(tmpString + "\n");
                        }

                        String bdeger = sbuilder.toString();

                        rio.response_str = bdeger;

                        try {
                            JSONObject object = new JSONObject(bdeger);
                            rio.response = object;
                        } catch (JSONException e) {
                            rio.response = null;
                            rio.exception += e.toString();
                        }



                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        e.printStackTrace();
                        rio = rio == null ? new RIO() : rio;
                        rio.exception += e.toString();
                        rio.result = HttpResponseType.HTTP_FORBIDDEN;
                        rio.responseUrl = _url;
                    } catch (IOException e) {
                        e.printStackTrace();
                        rio = rio == null ? new RIO() : rio;
                        rio.exception += e.toString();
                        rio.result = HttpResponseType.HTTP_FORBIDDEN;
                        rio.responseUrl = _url;
                    }

                    _isBusy = false;

                    return rio;
                }

                @Override
                protected void onPostExecute(Object o) {
                    Looper.prepare();
                    _requestFactory.Happens((RIO)o);
                    super.onPostExecute(o);
                }
            };
            task.execute();
            super.execute();
        }
    }

    private final class GetRequest extends  Request{
        public GetRequest(String url){
            _url = url;
            requestType = RequestType.GET;
        }

        @TargetApi(Build.VERSION_CODES.CUPCAKE)
        @Override
        public void execute() {

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {

                    _isBusy = true;
                    URL url = null;

                    RIO rio = new RIO();

                    try {
                        url = new URL(_url);
                        InputStream istream = null;
                        if(_isSecure){
                            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                            if(_timeout_on){
                                connection.setConnectTimeout(timeOut);
                            }

                            for (int i = 0 ; i< _requestHeaders.size(); i++){
                                String key = _requestHeaders.keys().nextElement();
                                String value = _requestHeaders.get(key);

                                connection.setRequestProperty(key,value);

                            }

                            connection.connect();

                            istream = connection.getInputStream();

                            rio.request = GetRequest.this;
                            rio.result = HttpRequestFactory.getResult(connection.getResponseCode());
                            rio.responseUrl = _url;
                        }
                        else{

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                            if(_timeout_on){
                                connection.setConnectTimeout(timeOut);
                            }

                            for (int i = 0 ; i< _requestHeaders.size(); i++){
                                String key = _requestHeaders.keys().nextElement();
                                String value = _requestHeaders.get(key);

                                connection.setRequestProperty(key,value);

                            }

                            connection.connect();

                            istream = connection.getInputStream();

                            rio.request = GetRequest.this;
                            rio.result = HttpRequestFactory.getResult(connection.getResponseCode());
                            rio.responseUrl = _url;
                        }




                        BufferedReader reader = new BufferedReader(new InputStreamReader(istream,_requestFactory.getCharset()));

                        StringBuilder stringBuilder = new StringBuilder();
                        String tmp = "";

                        while((tmp = reader.readLine()) != null){
                            stringBuilder.append(tmp + "\n");
                        }

                        String sdeger = stringBuilder.toString();

                        rio.response_str = sdeger;

                        try {
                            JSONObject jo = new JSONObject(sdeger);
                            rio.response = jo;
                        } catch (JSONException e) {
                            rio.response = null;
                            rio.exception += e.toString();
                            e.printStackTrace();
                        }

                    } catch (MalformedURLException e) {
                        rio.responseUrl = _url;
                        rio.exception += e.toString();
                        rio.result = HttpResponseType.HTTP_FORBIDDEN;
                        e.printStackTrace();
                    } catch (IOException e) {
                        rio.responseUrl = _url;
                        rio.exception += e.toString();
                        rio.result = HttpResponseType.HTTP_FORBIDDEN;
                        e.printStackTrace();
                    }

                    _isBusy = false;

                    return rio;
                }

                @Override
                protected void onPostExecute(Object o) {
                    _requestFactory.Happens((RIO)o);
                    super.onPostExecute(o);
                }
            };

            task.execute();
            super.execute();
        }
    }



    public Request createRequest(RequestType type, String url){

        switch (type){
            case POST:
                PostRequest pr = new PostRequest(url);
                pr._requestFactory = this;
                return pr;
            case GET:
                GetRequest gr = new GetRequest(url);
                gr._requestFactory = this;
                return gr;
            default:
                return null;

        }
    }

    public void executeRequest(RequestType type, String url, String body){

        switch (type){
            case POST:
                PostRequest pr = new PostRequest(url);
                pr.setBody(body);
                pr._requestFactory = this;
                pr.execute();
                break;
            case GET:
                GetRequest gr = new GetRequest(url);
                gr.setBody(body);
                gr._requestFactory = this;
                gr.execute();
                break;
        }
    }

    public void executeRequest(String url){
        GetRequest request = new GetRequest(url);
        request._requestFactory = this;
        request.execute();
    }

    public HttpRequestFactory(Context context){
        this.context = context;
    }

    public void setCharset(String charset){
        _charSet = charset;
    }

    public String getCharset(){
        return _charSet;
    }

    public abstract void Happens(RIO rio);
}
