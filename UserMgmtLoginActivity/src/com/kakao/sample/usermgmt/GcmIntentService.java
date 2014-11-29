/**
 * Copyright 2014 Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.sample.usermgmt;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kakao.helper.Logger;

/**
 * {@link com.kakao.sample.push.GcmBroadcastReceiver}로부터 전달 받은 메시지가 담긴 intent를 처리하는 서비스 구현체.
 * @author MJ
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    /**
     * 받은 메시지의 타입을 덧붙여 알림센터에 보여주고, 잡고 있던 wake lock를 풀어준다.
     * @param intent 전달 받은 메시지가 담긴 intenht.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        //gps받아와서 sendNotification 해주면 될
        if (!extras.isEmpty()) {
            Logger.getInstance().i(messageType + " - " + extras.toString());
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification("Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notifyIntent = new Intent(this, UserMgmtLoginActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.noti_icon)
        .setContentTitle(getApplicationContext().getString(R.string.notification_title))
        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
