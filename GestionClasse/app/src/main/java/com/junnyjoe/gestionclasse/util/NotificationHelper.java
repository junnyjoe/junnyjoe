package com.junnyjoe.gestionclasse.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.junnyjoe.gestionclasse.R;
import com.junnyjoe.gestionclasse.data.Message;
import com.junnyjoe.gestionclasse.ui.ConversationActivity;

/** Notification locale « nouveau message » ouvrant la conversation concernée. */
public final class NotificationHelper {
    private static final String CHANNEL_ID = "messages";
    private static final int PREVIEW_LENGTH = 80;

    private NotificationHelper() { }

    public static boolean canNotify(Context context) {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Affiche la notification destinée au destinataire du message.
     *
     * @return false si les notifications ne sont pas autorisées
     */
    @SuppressLint("MissingPermission")
    public static boolean notifyNewMessage(Context context, Message m, String senderName) {
        if (!canNotify(context)) return false;
        createChannel(context);

        // L'appui ouvre la conversation du point de vue du destinataire.
        Intent open = ConversationActivity.intent(context, m.destinataireId, m.expediteurId);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, (int) m.id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String preview = m.contenu.isEmpty() ? "📎" : m.contenu;
        if (preview.length() > PREVIEW_LENGTH) {
            preview = preview.substring(0, PREVIEW_LENGTH) + "…";
        } else if (m.hasAttachment() && !m.contenu.isEmpty()) {
            preview = preview + " 📎";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notif_title, senderName))
                .setContentText(preview)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(preview))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(context).notify((int) m.id, builder.build());
        return true;
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notif_channel_desc));
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
