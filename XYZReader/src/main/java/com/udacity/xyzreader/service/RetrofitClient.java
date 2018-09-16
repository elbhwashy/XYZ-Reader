package com.udacity.xyzreader.service;

import android.content.Context;
import android.util.Log;

import com.udacity.xyzreader.R;

import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = RetrofitClient.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static com.udacity.xyzreader.service.UdacityJsonClient sInstance;
    private static final Object OKHTTP_LOCK = new Object();
    private static OkHttpClient sOkHttpInstance;

    public static com.udacity.xyzreader.service.UdacityJsonClient getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "Creating new RetrofitClient instance");
                try {
                    URL url = new URL(context.getResources().getString(R.string.udacity_json_articles_source));
                    String baseUrl = url.getProtocol() + "://" + url.getHost();
                    sInstance = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(getOkHttpInstance())
                            .build().create(com.udacity.xyzreader.service.UdacityJsonClient.class);
                } catch (MalformedURLException e) {
                    Log.e(TAG, "getInstance: encountered problem with URL for articles data");
                }
            }
        }
        Log.d(TAG, "Getting the RetrofitClient instance");
        return sInstance;
    }

    // Will come handy for IdlingRegistry usage while androidTest`ing
    public static OkHttpClient getOkHttpInstance() {
        if (null == sOkHttpInstance) {
            synchronized (OKHTTP_LOCK) {
                sOkHttpInstance = new OkHttpClient.Builder().build();
            }
        }
        return sOkHttpInstance;
    }
}