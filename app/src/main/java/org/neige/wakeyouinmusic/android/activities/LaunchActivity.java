package org.neige.wakeyouinmusic.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startActivity(new Intent(this, AlarmListActivity.class));
	}
}
