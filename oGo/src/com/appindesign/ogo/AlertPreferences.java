package com.appindesign.ogo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
 
 public class AlertPreferences extends PreferenceActivity {

	private CheckBoxPreference SetSound;       
    private CheckBoxPreference SetVibration;
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {

       super.onCreate(savedInstanceState);
       addPreferencesFromResource(R.xml.alert_prefs);
       
       SetSound = (CheckBoxPreference) getPreferenceScreen().findPreference(getString(R.string.pref_sound_key));
       setSoundCheckBoxSummary(SetSound);
       
       SetVibration = (CheckBoxPreference) getPreferenceScreen().findPreference(getString(R.string.pref_vibration_key));
       setVibrationCheckBoxSummary(SetVibration);
       
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);     
       prefs.registerOnSharedPreferenceChangeListener(

       new OnSharedPreferenceChangeListener() {
          public void onSharedPreferenceChanged (SharedPreferences prefs, String key) {

             if (key.equals(getString(R.string.pref_sound_key))) {
                setSoundCheckBoxSummary(SetSound);
             }
             if (key.equals(getString(R.string.pref_vibration_key))) {
                setVibrationCheckBoxSummary(SetVibration);
             }
          }
       });
    }
 
 private void setSoundCheckBoxSummary (CheckBoxPreference pref) {
    if (pref.isChecked()) {
       pref.setSummary(R.string.pref_sound_on_summary);
    }
    else {
       pref.setSummary(R.string.pref_sound_off_summary);
    }
 }

 private void setVibrationCheckBoxSummary (CheckBoxPreference pref) {
    if (pref.isChecked()) {
       pref.setSummary(R.string.pref_vibration_on_summary);
    }
    else {
       pref.setSummary(R.string.pref_vibration_off_summary);
    }
 }
 
}