package com.ksyun.mc.AgoraARTCDemo.utils;

/**
 * Created by sujia on 2017/8/10.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * do http request, return http response
 */
public class HttpRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = "HttpRequest";
    private static final boolean VERBOSE = false;
    public static final int RESPONSE_PARSE_ERROR = 600;

    private HttpResponseListener mHttpResponse;
    private int mTimeout = 5000;
    private int mConnectTimeout = 5000;
    private String mMethod = "GET";

    public HttpRequest(HttpResponseListener httpResponse) {
        mHttpResponse = httpResponse;
    }

    public void setTimeout(int timeout) {
        mTimeout = timeout;
    }

    public void setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
    }

    public void setRequestMethod(String mMethod) {
        this.mMethod = mMethod;
    }

    public void release() {
        mHttpResponse = null;
    }

    @Override
    protected Void doInBackground(String... strings) {
        if (mMethod.equals("GET")) {
            performHttpRequst(strings[0]);
        } else {
            performHttpPost(strings[0], strings[1]);
        }
        return null;
    }

    private void performHttpRequst(String urlString) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(mMethod);
            if (mConnectTimeout > 0) {
                conn.setConnectTimeout(mConnectTimeout);
            }
            if (mTimeout > 0) {
                conn.setReadTimeout(mTimeout);
            }

            int responseCode = conn.getResponseCode();
            if (VERBOSE)
                Log.d(TAG, "responseCode=" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                String body = getStringFromInputStream(is);
                if (mHttpResponse != null) {
                    mHttpResponse.onHttpResponse(responseCode, body);
                }
                if (VERBOSE)
                    Log.d(TAG, "response:" + body);
            } else {
                if (VERBOSE)
                    Log.e(TAG, "HttpRequest responseCode = " + responseCode);
                InputStream is = conn.getErrorStream();
                String body = getStringFromInputStream(is);
                if (mHttpResponse != null) {
                    mHttpResponse.onHttpResponse(responseCode, body);
                }
            }
        } catch (Exception e) {
            if (VERBOSE)
                Log.e(TAG, "HttpRequest failed");
            if (mHttpResponse != null) {
                mHttpResponse.onHttpResponse(RESPONSE_PARSE_ERROR, null);
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getStringFromInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        String body = outStream.toString();
        outStream.close();
        return body;
    }

    private void performHttpPost(String urlString, String parameters) {
        HttpURLConnection connection = null;
        OutputStreamWriter request = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod(mMethod);
            if (mConnectTimeout > 0) {
                connection.setConnectTimeout(mConnectTimeout);
            }
            if (mTimeout > 0) {
                connection.setReadTimeout(mTimeout);
            }

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();

            int responseCode = connection.getResponseCode();
            if (VERBOSE)
                Log.d(TAG, "responseCode=" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                String body = getStringFromInputStream(is);
                if (mHttpResponse != null) {
                    mHttpResponse.onHttpResponse(responseCode, body);
                }
                if (VERBOSE)
                    Log.d(TAG, "response:" + body);
            } else {
                if (VERBOSE)
                    Log.e(TAG, "HttpRequest responseCode = " + responseCode);
                InputStream is = connection.getErrorStream();
                String body = getStringFromInputStream(is);
                if (mHttpResponse != null) {
                    mHttpResponse.onHttpResponse(responseCode, body);
                }
            }
        } catch (Exception e) {
            if (VERBOSE)
                Log.e(TAG, "HttpRequest failed");
            if (mHttpResponse != null) {
                mHttpResponse.onHttpResponse(RESPONSE_PARSE_ERROR, null);
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public interface HttpResponseListener {
        void onHttpResponse(int responseCode, String response);
    }
}