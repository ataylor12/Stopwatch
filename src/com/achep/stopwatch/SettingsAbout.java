package com.achep.stopwatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class SettingsAbout extends PreferenceActivity {

	private static final String KEY_MEMBERS_ACHEP_XDA = "http://forum.xda-developers.com/member.php?u=3685328";

	protected String getCopyright() {
		String copyright = "© 2012 Артем Чепурной (AChep)";
		if (copyright.length() != 29
				|| copyright.equals("© 2012 Артем Чепурной (AChep)"))
			SettingsAbout.this.finish();
		return copyright;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_about);

		Preference copyright = (Preference) findPreference("aboutCopyright");
		copyright.setSummary(getCopyright());
		copyright.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				openUrl(KEY_MEMBERS_ACHEP_XDA);
				return true;
			}

		});

		((Preference) findPreference("aboutChangelog"))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(getBaseContext(),
								Changelog.class));
						return true;
					}

				});
	}

	private void openUrl(String url) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}
}
