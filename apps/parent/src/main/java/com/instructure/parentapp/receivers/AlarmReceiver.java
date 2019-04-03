/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.SplashActivity;
import com.instructure.parentapp.database.DatabaseHandler;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "canvasParentNotificationChannel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra(Const.TITLE_TEXT);
        String subTitle = intent.getStringExtra(Const.SUBTITLE_TEXT);

        if(TextUtils.isEmpty(title)) {
            title = context.getResources().getString(R.string.app_name_parent);
        }
        if(TextUtils.isEmpty(subTitle)) {
            subTitle = "";
        }
        //set up the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_canvas_logo)
                        .setContentTitle(title)
                        .setContentText(subTitle);

        Intent resultIntent = new Intent(context, SplashActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);


        // Sets an ID for the notification
        // Make it unique based on the title and subtitle
        int mNotificationId = title.hashCode() + subTitle.hashCode();
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(context, CHANNEL_ID, notificationManager);
        // Builds the notification and issues it.
        notificationManager.notify(mNotificationId, builder.build());
    }


    private static void createNotificationChannel(Context context, String channelId, NotificationManager nm) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        //Prevents recreation of notification channel if it exists.
        List<NotificationChannel> channelList = nm.getNotificationChannels();
        for (NotificationChannel channel : channelList) {
            if (channelId.equals(channel.getId())) {
                return;
            }
        }

        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);

        //Create the channel and add the group
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        channel.enableLights(false);
        channel.enableVibration(false);

        //create the channel
        nm.createNotificationChannel(channel);
    }
    public void setAlarm(Context context, Calendar calendar, long assignmentId, String title, String subTitle) {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(Const.TITLE_TEXT, title);
        i.putExtra(Const.SUBTITLE_TEXT, subTitle);
        //set alarms here

        //get the row id and set it as the request code so it will be unique
        int alarmId = -1;
        DatabaseHandler mDatabaseHandler = new DatabaseHandler(context);
        try {
            mDatabaseHandler.open();
            alarmId = mDatabaseHandler.getRowIdByAssignmentId(assignmentId);
            mDatabaseHandler.close();
        } catch (SQLException e) {
            //can't find the alarmId
        }

        //verify that we have a valid alarm id
        if(alarmId != -1) {
            PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    public void cancelAlarm(Context context, long assignmentId, String title, String subTitle) {
        //need to create an intent that matches the one we want to cancel
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Const.TITLE_TEXT, title);
        intent.putExtra(Const.SUBTITLE_TEXT, subTitle);

        int alarmId = -1;
        DatabaseHandler mDatabaseHandler = new DatabaseHandler(context);
        try {
            mDatabaseHandler.open();
            alarmId = mDatabaseHandler.getRowIdByAssignmentId(assignmentId);
            mDatabaseHandler.close();
        } catch (SQLException e) {

        }

        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        alarmManager.cancel(sender);
    }
}
