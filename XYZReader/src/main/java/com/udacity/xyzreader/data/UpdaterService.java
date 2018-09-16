package com.udacity.xyzreader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.udacity.xyzreader.R;
import com.udacity.xyzreader.model.Article;
import com.udacity.xyzreader.service.RetrofitClient;
import com.udacity.xyzreader.service.UdacityJsonClient;
import com.udacity.xyzreader.service.Utilities;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdaterService extends IntentService {
    private static final String TAG = UpdaterService.class.getSimpleName();

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.udacity.demur.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.udacity.demur.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        UdacityJsonClient client = RetrofitClient.getInstance(getApplicationContext());
        client.listArticles(getResources().getString(R.string.udacity_json_articles_source))
                .enqueue(new Callback<List<Article>>() {
                    @Override
                    public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                        if (response.code() == 200 && null != response.body()) {
                            // Don't even inspect the intent, we only do one thing, and that's fetch content.
                            ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

                            Uri dirUri = ItemsContract.Items.buildDirUri();

                            // Delete all items
                            cpo.add(ContentProviderOperation.newDelete(dirUri).build());

                            for (Article article : response.body()) {
                                ContentValues values = new ContentValues();
                                values.put(ItemsContract.Items.SERVER_ID, article.getId());
                                values.put(ItemsContract.Items.AUTHOR, article.getAuthor());
                                values.put(ItemsContract.Items.TITLE, article.getTitle());
                                values.put(ItemsContract.Items.BODY, article.getBody());
                                values.put(ItemsContract.Items.THUMB_URL, article.getThumb());
                                values.put(ItemsContract.Items.PHOTO_URL, article.getPhoto());
                                values.put(ItemsContract.Items.ASPECT_RATIO, article.getAspect_ratio());
                                values.put(ItemsContract.Items.PUBLISHED_DATE, article.getPublished_date());
                                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
                            }
                            try {
                                getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
                            } catch (RemoteException | OperationApplicationException e) {
                                Log.e(TAG, "Error updating content.", e);
                            }
                        } else {
                            // Need broadcast this info to show a snack bar message in the app
                            Log.d(TAG, "Couldn't process response from server :-(");
                        }
                        removeRefreshingFlag();
                    }

                    @Override
                    public void onFailure(Call<List<Article>> call, Throwable t) {
                        if (Utilities.isOnline(getApplicationContext())) {
                            // Need broadcast this info to show a snack bar message in the app
                            Log.d(TAG, "Experienced problems connecting server :-(");
                        } else {
                            // Need broadcast this info to show a snack bar message in the app
                            Log.d(TAG, "No network detected, check Your connection :-(");
                        }
                        removeRefreshingFlag();
                    }
                });
    }

    private void removeRefreshingFlag() {
        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }
}