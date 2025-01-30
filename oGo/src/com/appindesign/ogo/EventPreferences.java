package com.appindesign.ogo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
 
 public class EventPreferences extends PreferenceActivity {
	 
	OnSharedPreferenceChangeListener listener;	 
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
       super.onCreate(savedInstanceState);

   	   SharedPreferences dspPrefs = PreferenceManager.getDefaultSharedPreferences(this);       
   	   int iEventNumber = dspPrefs.getInt(getString(R.string.pref_key_event), 1);
   	   
       switch (iEventNumber)
       {
       case 1:
            addPreferencesFromResource( R.xml.event_1_prefs );        	
        	break;
       case 2:
            addPreferencesFromResource( R.xml.event_2_prefs ); 
        	break;
       case 3:
            addPreferencesFromResource( R.xml.event_3_prefs ); 
        	break;
       case 4:
            addPreferencesFromResource( R.xml.event_4_prefs ); 
        	break;
        }
        
       populateSummaries();

       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);  		   
       listener = new OnSharedPreferenceChangeListener() 
	   {
	       public void onSharedPreferenceChanged (SharedPreferences prefs, String key) 
	       {
	    	   populateSummaries();
	       }
	   };
	   
	   preferences.registerOnSharedPreferenceChangeListener(listener);
    }
    
    @Override
    public void onDestroy()
    {
       super.onDestroy();
       SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
       preferences.unregisterOnSharedPreferenceChangeListener(listener);       
    } 
    
    public void populateSummaries()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this); 
       	SharedPreferences dspPrefs = PreferenceManager.getDefaultSharedPreferences(this);       
       	int iEventNumber = dspPrefs.getInt(getString(R.string.pref_key_event), 1);
       	
        //TODO - Pick this up from a global variable.
       	int iNUMBER_OF_COURSES = 8;
        int iCourseIndex = (iEventNumber-1)*iNUMBER_OF_COURSES ;

 	   	String color_description [] = getResources().getStringArray( R.array.color_description );
		String color_values [] =  getResources().getStringArray( R.array.color_value );

		String interval_description [] = getResources().getStringArray(R.array.interval_description);
		String interval_values [] =  getResources().getStringArray(R.array.interval_value);
		
        String key;
        String defaultValue;
        Boolean bDefaultValue;
        Preference preference;
        CheckBoxPreference cPreference;
        
        //Event Type
        key = getString( R.string.pref_key_event_type ) + iEventNumber; 
        defaultValue = getString( R.string.pref_title_event_type ) + iEventNumber;
 	   	preference = getPreferenceScreen().findPreference( key );
 	   	preference.setSummary( preferences.getString( key, defaultValue ) );
 	
    	for (int i = 1; i <= iNUMBER_OF_COURSES; i++)
    	{
    		//Course Names
            key = getString( R.string.pref_key_name_course ) + ( i+iCourseIndex ); 
            defaultValue = getString(R.string.default_course_name);
     	   	preference = getPreferenceScreen().findPreference( key );
     	   	if ( defaultValue.equals( preferences.getString( key, defaultValue ) ) )
     	   	{
     	   		preference.setSummary( R.string.pref_summary_course_name );
     	   	}
     	   	else
     	   	{
     	   	    preference.setSummary( preferences.getString( key, defaultValue ) );
     	   	}
    		
    		//Start Button Colours
            key = getString( R.string.pref_key_color_course ) + ( i+iCourseIndex ); 
            defaultValue = getString(R.string.default_start_button_color);
     	   	preference = getPreferenceScreen().findPreference( key );
     	   	preference.setSummary( preferences.getString( key, defaultValue ) );   
     	   	for( int w = 0; w < color_values.length; w++ ) 
    		{
    			if( color_values[w].equals( preferences.getString( key, defaultValue ) ) ) 
    			{
    				preference.setSummary( color_description[w] );
    			}
    		}   	   
  	   
    		//Intervals
            key = getString( R.string.pref_key_interval_course ) + ( i+iCourseIndex ); 
            defaultValue = getString(R.string.default_course_interval);
     	   	preference = getPreferenceScreen().findPreference( key );
     	   	preference.setSummary( preferences.getString( key, defaultValue ) );
        
    		for( int w = 0; w < interval_values.length; w++ ) 
    		{
    			if( interval_values[w].equals( preferences.getString( key, defaultValue ) ) )      		 
    			{
    				preference.setSummary(interval_description[w]);
    			}
    		}
    		
    		//Visibility
    		key = getString( R.string.pref_key_visibility_course ) + ( i+iCourseIndex );
    		defaultValue = getString(R.string.default_course_visibility);
    		cPreference = (CheckBoxPreference) getPreferenceScreen().findPreference( key );
    		cPreference.setSummary( getString( R.string.hide ) );
    		
    		if ( cPreference.isChecked() )
    		{
    			cPreference.setSummary( getString( R.string.show ) );
    		}
    		else
    		{
    			cPreference.setSummary( getString( R.string.hide ) );
    		}
    		
    	}   
    }
}