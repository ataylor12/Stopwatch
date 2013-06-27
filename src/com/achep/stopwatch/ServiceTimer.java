package com.achep.stopwatch;

import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;


public class ServiceTimer extends Service implements
		OnSharedPreferenceChangeListener {

	private static final String WAKELOCK_TAG = "ServiceTimer";

	private BroadcastReceiver mReceiver;
	private Context context;
	private AlarmManager mAlarm;
	private PendingIntent pIntent;
	private NotificationManager mNotificationManager;

	private long mStartTime;
	private long[][] list;
	private int length;

	private static final int NOTIFY_ID = 1;
	private static final int ICON = R.drawable.ic_statusbar_timer;
	private CharSequence mNotifyText;
	private long mTimeZone;
	private PowerManager.WakeLock cpuLockUp;
	private boolean mWakeLock;
	private boolean mTimerNotification;

	private void setNotification(String msg) {
		Notification notification = new Notification(ICON, mNotifyText, 0);
		CharSequence contentText = msg;
		Intent notificationIntent = new Intent(this, StopwatchActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(context, mNotifyText, contentText,
				contentIntent);
		mNotificationManager.notify(NOTIFY_ID, notification);
	}

	@Override
	public void onCreate() {
		context = getBaseContext();
		mReceiver = new BroadcastRec();

		// Load preferences
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(
				PreferenceManager.getDefaultSharedPreferences(this), "null");

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyText = getResources().getText(R.string.notification_timer);
		mTimeZone = TimeZone.getDefault().getRawOffset();

		IntentFilter filter = new IntentFilter(BroadcastRec.ACTION_TIMER);
		registerReceiver(mReceiver, filter);

		mAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		pIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				BroadcastRec.ACTION_TIMER), 0);

		loadTimerData();

		if (mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			cpuLockUp = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					WAKELOCK_TAG);
			cpuLockUp.acquire();
		}
	}

	private String getTimeFullLong(long time) {
		time += mTimeZone;
		time = (time - time % 1000) / 1000;
		time = (time - time % 60) / 60;
		long h = ((time - time % 60) / 60) % 24;
		return Utils
				.addZero(!DateFormat.is24HourFormat(context) && h > 12 ? h - 12
						: h)
				+ ":" + Utils.addZero(time % 60);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		this.stopService(new Intent(this, ServiceKlaxon.class));
		mNotificationManager.cancel(NOTIFY_ID);
		if (mWakeLock)
			cpuLockUp.release();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		mAlarm.set(AlarmManager.RTC_WAKEUP, getNextTimer(), pIntent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private long getNextTimer() {
		long min = Long.MAX_VALUE;
		long time = System.currentTimeMillis() - this.mStartTime;
		for (int i = 0; i < length; i++) {
			if (list[i][1] == -1 || list[i][0] > time) {
				if (list[i][0] < min && list[i][0] > time)
					min = list[i][0];
			} else {
				final long x = list[i][1] == 0 ? (Math.round(time / list[i][0]
						+ 0.5) * list[i][0]) : (Math.round((time - list[i][0])
						/ (list[i][1] + list[i][0]) + 0.5)
						* (list[i][1] + list[i][0]) + list[i][0]);
				if (x < min && x > time)
					min = x;
			}
		}
		if (min == Long.MAX_VALUE)
			stopSelf();
		else if (mTimerNotification)
			setNotification(getTimeFullLong(min + mStartTime));
		return min + mStartTime;
	}

	private void loadTimerData() {
		SharedPreferences mData = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (mData != null) {
			if (mData.getBoolean(Utils.KEY_TIMER_UNRUNNING, true))
				stopSelf();
			else {
				mStartTime = mData
						.getLong(Utils.KEY_TIMER_START, 0);
				if (mStartTime == 0)
					stopSelf();
				else {
					length = mData
							.getInt(Utils.KEY_TIMER_TIMERS, 0);
					if (length > 0) {
						list = new long[length][2];
						for (int i = 0; i < length; i++) {
							list[i][0] = Utils.getTime(mData.getString(
									Utils.KEY_TIMER_PRIMARY_LINE
											+ i, "00:00:00"))*1000;
							String str2 = mData.getString(
									Utils.KEY_TIMER_SECONDARY_LINE
											+ i, "");
							list[i][1] = str2.equals("") ? -1 : Utils
									.getTime(str2)*1000;
						}
					} else
						stopSelf();
				}
			}
		} else
			stopSelf();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		mWakeLock = sharedPreferences.getBoolean(
				Utils.KEY_TIMER_WAKELOCK_ENABLED, false);
		mTimerNotification = sharedPreferences.getBoolean(
				Utils.KEY_TIMER_NOTIFICATIONS_ENABLED, true);
		if (!mTimerNotification) 
			mNotificationManager.cancel(NOTIFY_ID);		
	}
}
