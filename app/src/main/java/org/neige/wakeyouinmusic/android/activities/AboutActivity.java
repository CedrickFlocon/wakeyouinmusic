package org.neige.wakeyouinmusic.android.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.neige.wakeyouinmusic.android.R;
import org.neige.wakeyouinmusic.android.fragments.AboutFragment;

public class AboutActivity extends ActionBarActivity implements AboutFragment.OnFragmentInteractionListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		if (savedInstanceState == null){
			getFragmentManager().beginTransaction().replace(R.id.fragment,AboutFragment.newInstance()).commit();
		}
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
