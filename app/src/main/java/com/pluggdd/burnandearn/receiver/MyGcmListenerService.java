package com.pluggdd.burnandearn.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.gcm.GcmListenerService;
import com.pluggdd.burnandearn.R;
import com.pluggdd.burnandearn.utils.PreferencesManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

/**
 * Created by User on 19-Mar-16.
 */
public class MyGcmListenerService extends GcmListenerService {

    public static final int MESSAGE_NOTIFICATION_ID = 10000;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.i("GCM Message",from + " " + data.toString());
        new ProfileImageTask(data.getString("title"),data.getString("message")).execute();
        //createNotification(data.getString("title"),data.getString("message"));
    }

    private class ProfileImageTask extends AsyncTask<Void,Bitmap,Bitmap>{

        private String mTitle,mMessage;

        public ProfileImageTask(String title,String message){
            mTitle = title;
            mMessage = message;
        }


        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                return Picasso.with(MyGcmListenerService.this).load(new PreferencesManager(MyGcmListenerService.this).getStringValue(getString(R.string.profile_image_url))).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Context context = getBaseContext();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(mTitle)
                    .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    .setContentText(mMessage);
            NotificationManager mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if(bitmap != null)
                mBuilder.setLargeIcon(bitmap);
            mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
        }
    }

}
