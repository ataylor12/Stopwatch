package com.achep.stopwatch;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Settings extends PreferenceActivity {

	private static final String FEEDBACK_FORM_URL = "https://docs.google.com/spreadsheet/viewform?formkey=dFZ4Nnp2YldvTFhTeFo1LWxwclZtcnc6MA";

	private static final String KEY_SETTINGS_FEEDBACK = "settingsFeedback";
	private static final String KEY_SETTINGS_ABOUT = "settingsAbout";

	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		addPreferencesFromResource(R.xml.settings);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		((Preference) findPreference(KEY_SETTINGS_FEEDBACK))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri
								.parse(FEEDBACK_FORM_URL)));
						return true;
					}

				});
		((Preference) findPreference(KEY_SETTINGS_ABOUT))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getBaseContext(),
								SettingsAbout.class));
						return true;
					}

				});

		((Preference) findPreference(Utils.KEY_SETTINGS_LOCALE))
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String value = newValue.toString();
						Locale locale = value.equals("system") ? Locale
								.getDefault() : new Locale(value);
						Locale.setDefault(locale);
						Configuration config = new Configuration();
						config.locale = locale;
						context.getResources().updateConfiguration(config,
								context.getResources().getDisplayMetrics());
						Toast.makeText(context,
								getResources().getText(R.string.locale_toast),
								Toast.LENGTH_SHORT).show();
						return true;
					}

				});
		final Preference stopwatchMode = (Preference) findPreference(Utils.KEY_STOPWATCH_MODE);
		stopwatchMode
				.setSummary(getResources()
						.getString(
								settings.getBoolean(Utils.KEY_STOPWATCH_MODE, true) ? R.string.stopwatch_textmode2
										: R.string.stopwatch_textmode3));
		stopwatchMode
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						stopwatchMode.setSummary(getResources()
								.getString(
										newValue.equals(true) ? R.string.stopwatch_textmode2
												: R.string.stopwatch_textmode3));
						return true;
					}

				});
	}
}
