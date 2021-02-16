package com.easyrequest.apieasyrequest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;


import com.easyrequest.apieasyrequest.enums.Method;
import com.easyrequest.apieasyrequest.response.Response;
import com.easyrequest.apieasyrequest.tools.TrustAllCerts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestManager {

    private OkHttpClient mClient;
    private Handler mHandler;

    private static RequestManager sRequestManager;
    private static String BASE_URL;
    private static Context mContext;


    private RequestManager() {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                .hostnameVerifier((hostname, session) -> true)
                .build();

        mHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized void init(Context pContext, String baseUrl) {
        if (sRequestManager == null) {
            sRequestManager = new RequestManager();
            mContext = pContext;
            BASE_URL = baseUrl;
        }

    }

    public static RequestManager getInstance() {
        return sRequestManager;
    }

    private void request(String url, Method method, @Nullable RequestBody body, Response pResponse) {
        request(url, method, null, body, pResponse);
    }

    private void request(String url, Method method, HashMap<String, String> headers, @Nullable RequestBody body, final Response pResponse) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .method(method.name(), body);
        if (headers != null) {
            for (String key : headers.keySet()) {
                String header = headers.get(key);
                if (header != null)
                    builder.addHeader(key, header);
            }
        }

        Request request = builder.build();

        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (pResponse != null) {
                    mHandler.post(() -> pResponse.onError(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) {
                if (pResponse != null) {
                    String data = "";
                    try {
                        data = response.body() == null ? "" : response.body().string();
                    } catch (Exception pE) {
                        pE.printStackTrace();
                    }
                    final String finalData = data;
                    mHandler.post(() -> {
                        int code = response.code();
                        if (code >= 200 && code < 300 || code == 304)
                            pResponse.onResponse(finalData);
                        else {
                            pResponse.onError(finalData);
                        }
                    });
                }

            }
        });
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }


    public interface OnResponse<T extends Object> {
        void onResponse(T pResponse);
    }


    public void doGetRequest(
            String path,
            Response pResponse) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        String url = BASE_URL + path;
        request(url, Method.GET, headers, null, pResponse);


    }

    public void doGetRequest(
            String path,
            HashMap<String, Object> params,
            Response pResponse) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        String getParamsPath = "";
        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            getParamsPath = getParamsPath + "&" + key + "=" + value;
        }
        if (getParamsPath.length() > 0)
            getParamsPath = "/?" + getParamsPath.substring(1);
        String url = BASE_URL + "/" + path + getParamsPath;
        request(url, Method.GET, headers, null, pResponse);
    }

    public void doGetRequest(
            String path,
            HashMap<String, Object> params,
            HashMap<String, String> headers,
            Response pResponse) {
        //headers.put("content-type", "application/json");
        String getParamsPath = "";
        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            getParamsPath = getParamsPath + "&" + key + "=" + value;
        }
        if (getParamsPath.length() > 0)
            getParamsPath = "/?" + getParamsPath.substring(1);
        String url = BASE_URL + "/" + path + getParamsPath;
        request(url, Method.GET, headers, null, pResponse);
    }


    public void doPostRequestFormBody(
            String path,
            HashMap<String, Object> params,
            Response pResponse) {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        RequestBody formBody;
        FormBody.Builder builder = new FormBody.Builder();

        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.add(key, value.toString());
        }
        formBody = builder.build();

        request(BASE_URL + "/" + path, Method.POST, headers, formBody, pResponse);

    }

    public void doPostRequestFormBody(
            String path,
            HashMap<String, Object> params,
            HashMap<String, String> headers,
            Response pResponse) {

        RequestBody formBody;
        FormBody.Builder builder = new FormBody.Builder();

        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.add(key, value.toString());
        }
        formBody = builder.build();

        request(BASE_URL + "/" + path, Method.POST, headers, formBody, pResponse);

    }


    public void doPostRequest(
            String path,
            HashMap<String, Object> params,
            Response pResponse) {
        JSONObject dataJson = new JSONObject();
        try {
            for (HashMap.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                dataJson.put(key, value);
            }
        } catch (JSONException pE) {
            pE.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = FormBody.create(JSON, dataJson.toString());
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");

        request(BASE_URL + "/" + path, Method.POST, headers, body, pResponse);
    }

    public void doPostRequest(
            String path,
            HashMap<String, Object> params,
            HashMap<String, String> headers,
            Response pResponse) {
        JSONObject dataJson = new JSONObject();
        try {
            for (HashMap.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                dataJson.put(key, value);
            }
        } catch (JSONException pE) {
            pE.printStackTrace();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = FormBody.create(JSON, dataJson.toString());

        request(BASE_URL + "/" + path, Method.POST, headers, body, pResponse);
    }

}
