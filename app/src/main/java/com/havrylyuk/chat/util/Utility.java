package com.havrylyuk.chat.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;

import com.havrylyuk.chat.activity.MainActivity;
import com.havrylyuk.chat.R;

/**
 * Created by Igor Havrylyuk on 15.03.2017.
 */

public class Utility {

    private static final int CHAT_NOTIFICATION_ID = 3004;

    public static void showNotification(Context context, final String body, final String title) {
            Resources resources = context.getResources();
            int iconId = android.R.drawable.ic_menu_send;
            int artResourceId = R.mipmap.ic_launcher;
            Bitmap largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
            android.support.v7.app.NotificationCompat.Builder mBuilder =
                    (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(context)
                            .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                            .setSmallIcon(iconId)
                            .setLargeIcon(largeIcon)
                            .setAutoCancel(true)
                            .setContentTitle(title)
                            .setContentText(body);
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent( 0,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(CHAT_NOTIFICATION_ID, mBuilder.build());
        }


}
