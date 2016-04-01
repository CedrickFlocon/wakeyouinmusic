package org.neige.wakeyouinmusic.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.fragments.SettingFragment;

public class SettingActivity extends ActionBarActivity {

	private SettingFragment settingFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		settingFragment = SettingFragment.newInstance();

		getFragmentManager().beginTransaction().replace(R.id.content, settingFragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.activity_transition_from_left, R.anim.activity_transition_to_right);
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_transition_from_left, R.anim.activity_transition_to_right);
	}
}
