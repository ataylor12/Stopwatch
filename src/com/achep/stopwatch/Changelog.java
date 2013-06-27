package com.achep.stopwatch;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Changelog extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelog);
		

        TabHost tabHost = getTabHost();
                                           
        
        TabSpec last = tabHost.newTabSpec("tab1");             
        last.setIndicator(getString(R.string.changelog_last));                           
        last.setContent(new Intent(this, ChangelogContent.class).putExtra("changelog", 1));  
        
        TabSpec all = tabHost.newTabSpec("tab2");             
        all.setIndicator(getString(R.string.changelog_all));    
        all.setContent(new Intent(this, ChangelogContent.class).putExtra("changelog", 2));

        
        tabHost.addTab(last);                                      
        tabHost.addTab(all);   
	}
}