package com.appindesign.ogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Ogo extends Activity {
 
 //Constants
 private final int iNUMBER_OF_EVENTS = 4;
 private final int iNUMBER_OF_COURSES = 8;
 private final int iVIBRATE_DURATION = 500;
 
 //Menu options
 private final int iMENU_EVENT_TYPE = 1;
 private final int iMENU_EVENT_SETTINGS = 2;
 private final int iMENU_ZERO_EVENT = 3; 
 private final int iMENU_ALERTS = 4;
 private final int iMENU_DEFAULT_EVENT = 5;
 private final int iMENU_ABOUT = 6;
 
 //Count-down display
 private Button[] btnaCountDownDisplay = new Button[iNUMBER_OF_COURSES+1];
 //Runner image
 private ImageView[] ivaRunner = new ImageView[iNUMBER_OF_COURSES+1];
 //Start buttons
 private Button[] btnaStartButton = new Button[iNUMBER_OF_COURSES+1];
 //Course names
 private String sCourseNameDefault;
 private String[] saCourseNamePrefsKey = new String[iNUMBER_OF_COURSES*iNUMBER_OF_EVENTS+1];
 //Course button colours
 private String sStartButtonColorDefault;
 private String[] saStartButtonColorPrefsKey = new String[iNUMBER_OF_COURSES*iNUMBER_OF_EVENTS+1];
 //Course intervals
 private String sCourseIntervalDefault;
 private String[] saCourseIntervalPrefsKey = new String[iNUMBER_OF_COURSES*iNUMBER_OF_EVENTS+1];
 private String[] saCourseInterval = new String[iNUMBER_OF_COURSES+1];
 //Course visibility
 private Boolean bCourseVisibilityDefault;
 private String[] saCourseVisibilityPrefsKey = new String[iNUMBER_OF_COURSES*iNUMBER_OF_EVENTS+1];
 
 //Starts text view
 private TextView tvEventType;
 private TextView tvStarts; 
 
 //Timers
 private Intervaltimer[] icaIntervalCounter = new Intervaltimer[iNUMBER_OF_COURSES+1]; 
 private final int iTICK_PERIOD = 100;
 
 //Variables
 private String[] sEventNames = new String[5];
 private String sEventType;
 private int iEventNumber;
 private int iCourseIndex;
 private int iStarts;
 private boolean bStartsLocked = false; 
 
 //Other objects
 private MediaPlayer mpPlayer;
 private OnSharedPreferenceChangeListener lrPreferenceChange;
 private Vibrator vVibrationAlert;
 
 @Override
 public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);
	 
	//Pick up default values
	sCourseNameDefault = getString(R.string.default_course_name);
	sStartButtonColorDefault = getString(R.string.default_start_button_color); 
	sCourseIntervalDefault = getString(R.string.default_course_interval);
	bCourseVisibilityDefault = true;

	//Find the event type and starts count views
	tvEventType = (TextView) findViewById(R.id.Event);
	tvStarts = (TextView) findViewById(R.id.Starts);	
	
	//Find the count-down displays, men and start buttons.
	for (int i = 1; i <= iNUMBER_OF_COURSES; i++) {
	   btnaCountDownDisplay[i] = (Button) this.findViewById(getResources().getIdentifier("Count"+i,"id",getPackageName()));
	   ivaRunner[i] = (ImageView) this.findViewById(getResources().getIdentifier("Man"+i,"id",getPackageName()));
	   btnaStartButton[i] = (Button) this.findViewById(getResources().getIdentifier("Start"+i,"id",getPackageName()));
	}
	
	applyPreferences();
	zeroEvent();

    //Create a listener for each start button
    for (int iCourseNumber = 1; iCourseNumber <= iNUMBER_OF_COURSES; iCourseNumber++) {
       btnaStartButton[iCourseNumber].setOnClickListener (new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             if (bStartsLocked) {explainLock();}
             else {
	            Button clickedButton = (Button) v;
	            for (int j = 1; j <= iNUMBER_OF_COURSES; j++) {
		            /*The following line doesn't work as a way of locking buttons with no name. It seems that when
	            	you set a name and then delete it that name is no longer a null.*/
	            	//if ((btnaStartButton[j].getId() == clickedButton.getId()) && (clickedButton.getText()!="")) {
	            	if (btnaStartButton[j].getId() == clickedButton.getId()) {
		            //if (icaIntervalCounter[j] != null){icaIntervalCounter[j].cancel();};
	            	if (icaIntervalCounter[j] != null){
	            		icaIntervalCounter[j].cancel();	            		
            		};
	                  icaIntervalCounter[j] = new Intervaltimer(Integer.parseInt(saCourseInterval[j]),iTICK_PERIOD,j);
	                  icaIntervalCounter[j].start();
	                  ivaRunner[j].setImageResource(R.drawable.standingman);
	                  iStarts++; tvStarts.setText(Integer.toString(iStarts));
	               }
	            }
             }
       }});
    }

    //Create a listener for locking/unlocking the screen
    tvStarts.setOnClickListener (new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			if (bStartsLocked) {tvStarts.setBackgroundDrawable(getResources().getDrawable(R.drawable.bk_title));}
			else {tvStarts.setBackgroundDrawable(getResources().getDrawable(R.drawable.starts_locked));}
			bStartsLocked = !bStartsLocked;
		}
	});
    
    //Create a listener for preference changes. On change apply the new preferences.
    lrPreferenceChange = new SharedPreferences.OnSharedPreferenceChangeListener() {		
       @Override
       public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
          applyPreferences();
       }
    };
    //Register the preference change listener against the default shared preferences.
    SharedPreferences dspApp = PreferenceManager.getDefaultSharedPreferences(this);
    dspApp.registerOnSharedPreferenceChangeListener(lrPreferenceChange);
 }

 @Override
 public void onDestroy() {
    super.onDestroy();
    killCounters();
    killBeeps();
 }

 //The next methods create the options menu.
 @Override
 public boolean onCreateOptionsMenu(Menu menu){
    super.onCreateOptionsMenu(menu);
    createOptionsMenu(menu);  		
    return true;
 }

 private void createOptionsMenu(Menu menu) {
    MenuItem mnu6 = menu.add(0, iMENU_EVENT_TYPE, iMENU_EVENT_TYPE, getString(R.string.menu_select_event));
    MenuItem mnu1 = menu.add(0, iMENU_EVENT_SETTINGS, iMENU_EVENT_SETTINGS, getString(R.string.menu_event_settings));
    MenuItem mnu7 = menu.add(0, iMENU_ZERO_EVENT, iMENU_ZERO_EVENT, getString(R.string.menu_zero_event));
    MenuItem mnu3 = menu.add(0, iMENU_ALERTS, iMENU_ALERTS, getString(R.string.menu_alerts));
    MenuItem mnu2 = menu.add(0, iMENU_DEFAULT_EVENT, iMENU_DEFAULT_EVENT, getString(R.string.menu_default_event));
    MenuItem mnu4 = menu.add(0, iMENU_ABOUT, iMENU_ABOUT, getString(R.string.menu_about));
    //mnu4.setIcon(R.drawable.ic_menu_info_details);
 } 
 
 @Override
 public boolean onOptionsItemSelected(MenuItem item)
    {return optionsMenuChoice(item);}

private boolean optionsMenuChoice(MenuItem item){
	
	switch (item.getItemId()){
		case iMENU_EVENT_SETTINGS:
			Intent j = null;
			j = new Intent(this, EventPreferences.class);
			startActivity(j);
			return true;
		case iMENU_DEFAULT_EVENT:
			AlertDialog.Builder adbDefaultEventConfirmation = new AlertDialog.Builder(this);
			adbDefaultEventConfirmation.setTitle(getString(R.string.Warning));
			adbDefaultEventConfirmation.setIcon(R.drawable.ogologo);
			adbDefaultEventConfirmation.setMessage(getString(R.string.event_default_warning));
			adbDefaultEventConfirmation.setCancelable(true);
			adbDefaultEventConfirmation.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton) {
				defaultEvent();
				zeroEvent();
			}});
			adbDefaultEventConfirmation.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int whichButton) {dialog.cancel();}});
			adbDefaultEventConfirmation.show();
			return true;
		case iMENU_ALERTS:
			Intent i = new Intent(this, AlertPreferences.class);
			startActivity(i);
			return true;
		case iMENU_ABOUT:
			PackageManager pm = getPackageManager(); 
			try {PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
				AlertDialog.Builder adbAbout = new AlertDialog.Builder(this);
				adbAbout.setTitle(getString(R.string.app_name)+ " " + getString(R.string.version) + " " + pi.versionName);
				adbAbout.setIcon(R.drawable.ogologo);
				adbAbout.setMessage(R.string.copyright);
				adbAbout.setCancelable(true);
				adbAbout.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton) {dialog.cancel();}});
				adbAbout.show();
	   		} catch (NameNotFoundException e) {}
			return true;	
		case iMENU_EVENT_TYPE:
			SharedPreferences dspAppPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			AlertDialog.Builder adbEventType = new AlertDialog.Builder(this);
			adbEventType.setTitle(R.string.menu_select_event);
			adbEventType.setCancelable(true);			
			adbEventType.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton) {dialog.cancel();}});
			for (int k = 1; k <= iNUMBER_OF_EVENTS; k++) {
			   sEventNames[k] = dspAppPrefs.getString(getString(R.string.pref_key_event_type)+Integer.toString(k),getString(R.string.event_type)+" "+Integer.toString(k));}
			final CharSequence[] csaEventNames = {sEventNames[1],sEventNames[2],sEventNames[3],sEventNames[4]};			 
			adbEventType.setSingleChoiceItems(csaEventNames, iEventNumber-1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					int iOldEventNumber = iEventNumber;
					iEventNumber = item + 1;
					switchToEvent(iEventNumber);
					if (iOldEventNumber != iEventNumber) {zeroEvent();};
					dialog.cancel();}
			});
			adbEventType.show();
			return true;
		case iMENU_ZERO_EVENT:
			zeroEvent();
			return true;
	}
	return false;
}

 public void applyPreferences() {

	//Retrieve the preferences cache to use in initialisation of values.
	SharedPreferences dspPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	
	//Find the preferred event and deduce the index number of its first course.
	iEventNumber = dspPrefs.getInt(getString(R.string.pref_key_event), 1);
	iCourseIndex = (iEventNumber-1)*iNUMBER_OF_COURSES;
	
	//Find the preference keys for course name, button colour and interval.
	for (int i = 1; i <= iNUMBER_OF_COURSES; i++) {
	   saCourseNamePrefsKey[i] = getString(R.string.pref_key_name_course)+(i+iCourseIndex);
	   saStartButtonColorPrefsKey[i] = getString(R.string.pref_key_color_course)+(i+iCourseIndex);
	   saCourseIntervalPrefsKey[i] = getString(R.string.pref_key_interval_course)+(i+iCourseIndex); 
	   saCourseVisibilityPrefsKey[i] = getString(R.string.pref_key_visibility_course)+(i+iCourseIndex);
	}

	//Populate the event type
	sEventType = dspPrefs.getString(getString(R.string.pref_key_event_type)+iEventNumber,getString(R.string.event_type)+" "+iEventNumber);
	tvEventType.setText(sEventType);	

	//Populate the course names, apply the button colour, set the intervals, show or hide courses.
	for (int i = 1; i <= iNUMBER_OF_COURSES; i++) {
	   btnaStartButton[i].setText(dspPrefs.getString(saCourseNamePrefsKey[i],sCourseNameDefault));
	   btnaStartButton[i].setBackgroundResource
	      (getResources().getIdentifier(
	         dspPrefs.getString(saStartButtonColorPrefsKey[i],sStartButtonColorDefault),
	         "drawable",getPackageName()));
	   saCourseInterval[i] = dspPrefs.getString(saCourseIntervalPrefsKey[i],sCourseIntervalDefault);
	   //Show or hide courses.
	   if (dspPrefs.getBoolean(saCourseVisibilityPrefsKey[i], bCourseVisibilityDefault)) 
	   {	
		   findViewById(getResources().getIdentifier("Row"+i,"id",getPackageName())).setVisibility(View.VISIBLE);
       }
	   else
	   {
		   findViewById(getResources().getIdentifier("Row"+i,"id",getPackageName())).setVisibility(View.GONE);
	   }	   
	}	
 }	

 public void zeroEvent() {
	
	killCounters();
	//Reset the count-down displays, men and start buttons.
	for (int i = 1; i <= iNUMBER_OF_COURSES; i++) {
	   btnaCountDownDisplay[i].setText(R.string.zero);
	   ivaRunner[i].setImageResource(R.drawable.runningman);
	} 
	//Next line caused a force close when called a second time.
	//killBeeps();
	iStarts = 0; tvStarts.setText(" ");
 }

 public void alertSound(int iBeep){
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (prefs.getBoolean(getString(R.string.pref_sound_key), true)){
       if ((mpPlayer != null) && mpPlayer.isPlaying()) { mpPlayer.stop(); mpPlayer.release();}   
       mpPlayer = MediaPlayer.create(this,iBeep);
       mpPlayer.start();
    }
 }

 public void alertVibrate(){
	 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	 if (prefs.getBoolean(getString(R.string.pref_vibration_key),true)){
	     if (vVibrationAlert != null) {vVibrationAlert = null;}
	     vVibrationAlert = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	     vVibrationAlert.vibrate(iVIBRATE_DURATION);
	  }
 }
 
 public void switchToEvent(int iNewEvent) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor editor = prefs.edit();	
    editor.putInt(getString(R.string.pref_key_event),iNewEvent);
    editor.commit();
    return;
 }

 public class Intervaltimer extends CountDownTimer {
    
    private int iCourse;
    public boolean bCounterStarted = false;
    
    public Intervaltimer(long lStartInterval, long lTickPeriod, int itheRow) {
       super(lStartInterval,lTickPeriod);
       iCourse = itheRow;}
    
    public void onTick(long lMillisUntilFinished){
       btnaCountDownDisplay[iCourse].setText(Long.toString((1000+lMillisUntilFinished)/1000));
       if ((lMillisUntilFinished<4000)&&(!bCounterStarted)) {
    	   bCounterStarted = true;
    	   alertSound(R.raw.bingbong2);
    	   //alertVibrate();
    	   ivaRunner[iCourse].setImageResource(R.drawable.getready);
	   }
   }
    
    public void onFinish(){
       btnaCountDownDisplay[iCourse].setText(R.string.zero);
       ivaRunner[iCourse].setImageResource(R.drawable.runningman);
       //if ((saCourseInterval[iCourse] == "10000") && (iCourse==8)) playBeep(R.raw.chariots); else 
       //alertSound(R.raw.bingbonggo);
       alertVibrate();
       bCounterStarted=false;
       }
 }

 public void killCounters() {
    for (int i = 1; i <= iNUMBER_OF_COURSES; i++) {
       if (icaIntervalCounter[i] != null) {icaIntervalCounter[i].cancel();}; 
    }
 }
 
 public void killBeeps() {
     if ((mpPlayer != null) && mpPlayer.isPlaying()) { mpPlayer.stop(); mpPlayer.release();}
 }

 public void defaultEvent() {
	SharedPreferences dspAppPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	SharedPreferences.Editor editor = dspAppPrefs.edit();
	for (int iRow = 1; iRow <= iNUMBER_OF_COURSES; iRow++) {
		editor.putString(getString(R.string.pref_key_event_type) + iEventNumber,getString(R.string.event_type)+" "+iEventNumber);
		editor.putString(saCourseNamePrefsKey[iRow], sCourseNameDefault);				
		editor.putString(saCourseIntervalPrefsKey[iRow], sCourseIntervalDefault);
		editor.putString(saStartButtonColorPrefsKey[iRow], sStartButtonColorDefault);
		editor.putBoolean(saCourseVisibilityPrefsKey[iRow], bCourseVisibilityDefault );
		editor.commit();}	
 }

 public void explainLock() {
		AlertDialog.Builder adbExplainLockedStarts = new AlertDialog.Builder(this);
		adbExplainLockedStarts.setTitle(getString(R.string.title_starts_locked));
		adbExplainLockedStarts.setIcon(R.drawable.ogologo);
		adbExplainLockedStarts.setMessage(getString(R.string.how_to_unlock_starts));
		adbExplainLockedStarts.setPositiveButton(getString(R.string.OK), 
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int whichButton) {dialog.cancel();}						
				});
		adbExplainLockedStarts.show();
 }
 
}
