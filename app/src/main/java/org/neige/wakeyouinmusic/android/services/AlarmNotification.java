package org.neige.wakeyouinmusic.android.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.models.Alarm;
import org.neige.wakeyouinmusic.android.players.Track;

public class AlarmNotification {

	private Notification notification;
	private Context context;
	private Alarm alarm;

    public AlarmNotification(Context context, Alarm alarm) {
        this.context = context;
        this.alarm = alarm;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(alarm.getLabel())
				.setContentText(alarm.getRingtone().getTitle())
                .setContentIntent(getDisplayUiPendingIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = Notification.PRIORITY_MAX;
        }
    }

	public Notification getNotification() {
		return notification;
	}

	private void initPlaylistView() {
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_playlist);

		notification.contentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
		notification.contentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());
		notification.contentView.setOnClickPendingIntent(R.id.nextButton, getNextPendingIntent());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_playlist_expanded);

			notification.bigContentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
			notification.bigContentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());
			notification.bigContentView.setOnClickPendingIntent(R.id.previousButton, getPreviousPendingIntent());
			notification.bigContentView.setOnClickPendingIntent(R.id.nextButton, getNextPendingIntent());
		}
	}

	private void initErrorView() {
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_error);

		notification.contentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
		notification.contentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_error_expanded);

			notification.bigContentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
			notification.bigContentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());
		}
	}

	private void initDefaultView() {
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_default);

		notification.contentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
		notification.contentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_alarm_default_expanded);

			notification.bigContentView.setOnClickPendingIntent(R.id.snoozeButton, getSnoozePendingIntent());
			notification.bigContentView.setOnClickPendingIntent(R.id.dismissButton, getDismissPendingIntent());
		}
	}

	public void updatePlaylistView(Track track, int notificationId) {
		//Hack GC can clear all remote view allocation
		initPlaylistView();

		//Content View
		notification.contentView.setTextViewText(R.id.trackTitleTextView, track.getName());
		notification.contentView.setTextViewText(R.id.artisteTextView, track.getArtiste());
		notification.contentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());
		notification.contentView.setImageViewResource(R.id.coverImageView, R.drawable.ic_empty_cover);

		if (track.getCoverUrl() != null && track.getCoverUrl().length() > 0) {
			Picasso.with(context).load(track.getCoverUrl()).into(notification.contentView, R.id.coverImageView, notificationId, notification);
		}

		//Big View
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView.setTextViewText(R.id.trackTitleTextView, track.getName());
			notification.bigContentView.setTextViewText(R.id.artisteTextView, track.getArtiste());
			notification.bigContentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());
			notification.bigContentView.setImageViewResource(R.id.coverImageView, R.drawable.ic_empty_cover);

			if (track.getCoverUrl() != null && track.getCoverUrl().length() > 0) {
				Picasso.with(context).load(track.getCoverUrl()).into(notification.bigContentView, R.id.coverImageView, notificationId, notification);
			}
		}
	}

	public void updateErrorView(String errorMessage, Track track) {
		//Hack GC can clear all remote view allocation
		initErrorView();

		//Content View
		notification.contentView.setTextViewText(R.id.trackTitleTextView, track.getName());
		notification.contentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());
		notification.contentView.setTextViewText(R.id.errorTextView, errorMessage);

		//Big View
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView.setTextViewText(R.id.trackTitleTextView, track.getName());
			notification.bigContentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());
			notification.bigContentView.setTextViewText(R.id.errorTextView, errorMessage);
		}
	}

	public void updateDefaultView(Alarm alarm, Track track) {
		//Hack GC can clear all remote view allocation
		initDefaultView();

		//Content View
		notification.contentView.setTextViewText(R.id.trackTitleTextView, track.getName());
		notification.contentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());

		//Big View
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification.bigContentView.setTextViewText(R.id.trackTitleTextView, track.getName());
			notification.bigContentView.setTextViewText(R.id.alarmLabelTextView, alarm.getLabel());
		}

	}

	public PendingIntent getPreviousPendingIntent() {
		Intent previousIntent = new Intent(context, AlarmService.class);
		previousIntent.setAction(AlarmService.PREVIOUS_ACTION);
		return PendingIntent.getService(context, 0, previousIntent, 0);
	}

	public PendingIntent getNextPendingIntent() {
		Intent nextIntent = new Intent(context, AlarmService.class);
		nextIntent.setAction(AlarmService.NEXT_ACTION);
		return PendingIntent.getService(context, 0, nextIntent, 0);

	}

	public PendingIntent getDismissPendingIntent() {
		Intent dismissIntent = new Intent(context, AlarmService.class);
		dismissIntent.setAction(AlarmService.DISMISS_ACTION);
		return PendingIntent.getService(context, 0, dismissIntent, 0);

	}

	public PendingIntent getSnoozePendingIntent() {
		Intent snoozeIntent = new Intent(context, AlarmService.class);
		snoozeIntent.setAction(AlarmService.SNOOZE_ACTION);
		return PendingIntent.getService(context, 0, snoozeIntent, 0);
	}

	public PendingIntent getDisplayUiPendingIntent() {
		Intent displayUiIntent = new Intent(context, AlarmService.class);
		displayUiIntent.setAction(AlarmService.DISPLAY_UI_ACTION);
		return PendingIntent.getService(context, 0, displayUiIntent, 0);
	}

}
