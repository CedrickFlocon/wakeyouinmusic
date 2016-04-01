package org.neige.wakeyouinmusic.android.tests;


import android.test.AndroidTestCase;

import org.neige.wakeyouinmusic.android.R;

public class ResourceTest extends AndroidTestCase {

    /**
     * Test Snooze Value : snooze value should be a base 60
     */
    public void testSnoozeValue(){
        for (String snoozeDurationValue : getContext().getResources().getStringArray(R.array.snoozeDurationValue)) {
            int snoozeDuration = Integer.parseInt(snoozeDurationValue);
            assertEquals(snoozeDuration % 60, 0);
        }
    }

    /**
     * Test snooze and crescendo text and value
     */
    public void testPreferenceTextAndValue(){
        assertEquals(getContext().getResources().getStringArray(R.array.snoozeDurationValue).length, getContext().getResources().getStringArray(R.array.snoozeDuration).length);
        assertEquals(getContext().getResources().getStringArray(R.array.crescendoDurationValue).length, getContext().getResources().getStringArray(R.array.crescendoDuration).length);
    }
}
