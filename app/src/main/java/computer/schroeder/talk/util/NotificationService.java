package computer.schroeder.talk.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import computer.schroeder.talk.Main;
import computer.schroeder.talk.storage.SimpleStorage;
import computer.schroeder.talk.storage.entities.StoredConversation;
import computer.schroeder.talk.storage.entities.StoredMessage;

public class NotificationService
{
    /*public static void clear(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }*/

    public static void clear(Context context, long conversation)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel((int) conversation);
    }

    public static void update(Context context, ComplexStorageImpl complexStorage)
    {
        int localUser = new SimpleStorage(context).getUser();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("talk",
                    "Talk",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        List<StoredMessage> unread = complexStorage.getComplexStorage().messageSelectUnread();

        if(unread.size() == 0)
        {
            notificationManager.cancelAll();
            return;
        }

        HashMap<Long, ArrayList<StoredMessage>> messages = new HashMap<>();

        for(StoredMessage storedMessage : unread)
        {
            if(messages.get(storedMessage.getConversation()) == null) messages.put(storedMessage.getConversation(), new ArrayList<StoredMessage>());
            messages.get(storedMessage.getConversation()).add(storedMessage);
        }

        Notification summaryNotification =
                new NotificationCompat.Builder(context, "talk")
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setColor(Color.parseColor("#6a80ff"))
                        .setStyle(new NotificationCompat.InboxStyle().setSummaryText(unread.size() + " messages in " + messages.size() + " conversations"))
                        .setGroup("NEWMESSAGE")
                        .setGroupSummary(true)
                        .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                        .build();
        notificationManager.notify(0, summaryNotification);

        for(Long conversation : messages.keySet())
        {
            StoredConversation storedConversation = complexStorage.getConversation(conversation);
            ArrayList<StoredMessage> storedMessages = messages.get(conversation);

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

            for(StoredMessage storedMessage : storedMessages) style.addLine(complexStorage.getUser(storedMessage.getUser(), localUser).getUsername() + ": " + storedMessage.getMessage());

            style.setBigContentTitle(storedConversation.getTitle());


            Intent resultIntent = new Intent(context, Main.class);
            Bundle b = new Bundle();
            b.putLong("conversation", storedConversation.getId()); //Your id
            resultIntent.putExtras(b); //Put your id to your next Intent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT, b);

            Notification notification = new NotificationCompat.Builder(context, "talk")
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.tin))
                    .setColor(Color.parseColor("#6a80ff"))
                    .setContentTitle(storedConversation.getTitle() + ":")
                    .setContentText(complexStorage.getUser(storedMessages.get(0).getUser(), localUser).getUsername() + ": " + storedMessages.get(0).getMessage())
                    .setStyle(style)
                    .addAction(new NotificationCompat.Action(R.drawable.ic_stat_notification, "RESPONDE", pendingIntent))
                    .setContentIntent(pendingIntent)
                    .setGroup("NEWMESSAGE")
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    .build();
            notificationManager.notify(conversation.intValue(), notification);
        }
    }
}
