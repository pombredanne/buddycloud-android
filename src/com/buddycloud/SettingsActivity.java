package com.buddycloud;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.buddycloud.fragments.SettingsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;

public class SettingsActivity extends SherlockPreferenceActivity {

	public static final int REQUEST_CODE = 111;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		NotificationSettingsModel.getInstance().getFromServer(this, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				showPreferences(response);
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getApplicationContext(), 
						getString(R.string.message_notifications_settings_load_failed), 
						Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return SettingsFragment.onPreferenceClick(this, preference);
	}

	@SuppressWarnings("deprecation")
	protected void showPreferences(JSONObject response) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPrefs.edit();
		NotificationSettingsModel.getInstance().fillEditor(editor, response);
		editor.commit();
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preferences);
		} else {
			loadSettingsFragment();
		}
	}

	@SuppressLint("NewApi")
	private void loadSettingsFragment() {
		getFragmentManager().beginTransaction().replace(
				android.R.id.content, new SettingsFragment()).commit();
	}
}
