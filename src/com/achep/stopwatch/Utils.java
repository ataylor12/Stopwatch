package com.achep.stopwatch;

public class Utils {

	public static final String KEY_TIMER_UNRUNNING = "mTimerPaused";
	public static final String KEY_TIMER_START = "mTimerSystemTime";
	public static final String KEY_TIMER_TIMERS = "mTimerListLength";
	public static final String KEY_TIMER_PRIMARY_LINE = "mTimerListMain=";
	public static final String KEY_TIMER_SECONDARY_LINE = "mTimerListRepeat=";

	public static final String KEY_SETTINGS_LOCALE = "settingsLocale";
	public static final String KEY_TIMER_SOUND_ENABLED = "timerSound";
	public static final String KEY_TIMER_VIBRO_ENABLED = "timerVibro";
	public static final String KEY_TIMER_WAKELOCK_ENABLED = "timerWakeLock";
	public static final String KEY_TIMER_NOTIFICATIONS_ENABLED = "timerNotifications";
	public static final String KEY_STOPWATCH_MODE = "stopwatchTextMode";
	public static final String KEY_STOPWATCH_NOTIFICATIONS_ENABLED = "stopwatchNotifications";

	public static long getSecondTime(int h, int m, int s) {
		return s + (m + h * 60) * 60;
	}

	public static long getTime(String str) {
		int[] time = getLongFromStringTime(str);
		return Utils.getSecondTime(time[0], time[1], time[2]);
	}

	private static int[] getLongFromStringTime(String str) {
		int[] time = new int[3];
		int i = 0, c = 0, n = 0;
		while (n < 2) {
			if (str.charAt(i++) == ':') {
				if (n == 0) {
					time[n] = Integer.parseInt(str.substring(0, i - 1));
					c = i;
				} else {
					time[n] = Integer.parseInt(str.substring(c, i - 1));
					time[n + 1] = Integer.parseInt(str.substring(i,
							str.length()));
				}
				n++;
			}
		}
		return time;
	}

	public static String addZero(long x) {
		return String.format("%02d", x);
	}

}
