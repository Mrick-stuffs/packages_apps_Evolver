/*
 * Copyright (C) 2023 Evolution X
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolution.settings.fragments.notifications;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.evolution.settings.preference.CustomSeekBarPreference;
import com.evolution.settings.preference.SystemSettingListPreference;
import com.evolution.settings.preference.SystemSettingSwitchPreference;
import com.evolution.settings.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Notifications extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Notifications";

    private static final String ALERT_SLIDER_CAT = "alert_slider_cat";
    private static final String FLASH_ON_CALL_OPTIONS = "on_call_flashlight_category";
    private static final String FLASH_ON_NOTIFICATION_OPTIONS = "notification_flashlight_category";
    private static final String PREF_FLASH_ON_CALL = "flashlight_on_call";
    private static final String PREF_FLASH_ON_CALL_DND = "flashlight_on_call_ignore_dnd";
    private static final String PREF_FLASH_ON_CALL_RATE = "flashlight_on_call_rate";
    private static final String PREF_FLASH_ON_NOTIFY = "default_notification_torch";
    private static final String PREF_FLASH_ON_NOTIFY_TIMES = "default_notification_torch1";
    private static final String PREF_FLASH_ON_NOTIFY_RATE = "default_notification_torch2";

    private CustomSeekBarPreference mFlashOnCallRate;
    private CustomSeekBarPreference mFlashOnNotifyRate;
    private CustomSeekBarPreference mFlashOnNotifyTimes;
    private PreferenceCategory mAlertSliderCat;
    private TwoStatePreference mFlashOnNotify;
    private SystemSettingListPreference mFlashOnCall;
    private SystemSettingSwitchPreference mFlashOnCallIgnoreDND;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_notifications;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ContentResolver resolver = getActivity().getContentResolver();
        final Context mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = mContext.getResources();

        mAlertSliderCat = findPreference(ALERT_SLIDER_CAT);
        boolean mAlertSliderAvailable = res.getBoolean(
                com.android.internal.R.bool.config_hasAlertSlider);
        if (!mAlertSliderAvailable) {
            prefScreen.removePreference(mAlertSliderCat);
        }

        if (!DeviceUtils.deviceSupportsFlashLight(mContext)) {
            PreferenceCategory flashOnCallCategory = (PreferenceCategory)
                    findPreference(FLASH_ON_CALL_OPTIONS);
            PreferenceCategory flashOnNotifCategory = (PreferenceCategory)
                    findPreference(FLASH_ON_NOTIFICATION_OPTIONS);
            prefScreen.removePreference(flashOnCallCategory);
            prefScreen.removePreference(flashOnNotifCategory);
        } else {
            mFlashOnCallRate = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_CALL_RATE);
            int value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL_RATE, 1);
            mFlashOnCallRate.setValue(value);
            mFlashOnCallRate.setOnPreferenceChangeListener(this);

            mFlashOnCallIgnoreDND = (SystemSettingSwitchPreference)
                    findPreference(PREF_FLASH_ON_CALL_DND);
            value = Settings.System.getInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, 0);
            mFlashOnCallIgnoreDND.setVisible(value > 1);
            mFlashOnCallRate.setVisible(value != 0);

            mFlashOnCall = (SystemSettingListPreference)
                    findPreference(PREF_FLASH_ON_CALL);
            mFlashOnCall.setSummary(mFlashOnCall.getEntries()[value]);
            mFlashOnCall.setOnPreferenceChangeListener(this);

            mFlashOnNotifyTimes = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_NOTIFY_TIMES);
            mFlashOnNotifyRate = (CustomSeekBarPreference)
                    findPreference(PREF_FLASH_ON_NOTIFY_RATE);
            mFlashOnNotify = (TwoStatePreference)
                    findPreference(PREF_FLASH_ON_NOTIFY);
            String strVal = Settings.System.getStringForUser(resolver,
                    PREF_FLASH_ON_NOTIFY, UserHandle.USER_CURRENT);
            final boolean enabled = strVal != null && !strVal.isEmpty();
            mFlashOnNotify.setChecked(enabled);
            updateFlashOnNotifyValues(enabled, strVal);
            mFlashOnNotify.setOnPreferenceChangeListener(this);
            mFlashOnNotifyTimes.setOnPreferenceChangeListener(this);
            mFlashOnNotifyRate.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL, value);
            mFlashOnCall.setSummary(mFlashOnCall.getEntries()[value]);
            mFlashOnCallIgnoreDND.setVisible(value > 1);
            mFlashOnCallRate.setVisible(value != 0);
            return true;
        } else if (preference == mFlashOnCallRate) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.FLASHLIGHT_ON_CALL_RATE, value);
            return true;
        } else if (preference == mFlashOnNotify) {
            boolean value = (Boolean) newValue;
            if (!value) setFlashOnNotifyValues(0, 0);
            else setFlashOnNotifyValues(2, 2);
            return true;
        } else if (preference == mFlashOnNotifyTimes) {
            int value = (Integer) newValue;
            setFlashOnNotifyValues(value, mFlashOnNotifyRate.getValue());
            return true;
        } else if (preference == mFlashOnNotifyRate) {
            int value = (Integer) newValue;
            setFlashOnNotifyValues(mFlashOnNotifyTimes.getValue(), value);
            return true;
        }
        return false;
    }

    private void updateFlashOnNotifyValues(boolean enabled) {
        final String val = Settings.System.getStringForUser(
                getActivity().getContentResolver(),
                PREF_FLASH_ON_NOTIFY, UserHandle.USER_CURRENT);
        updateFlashOnNotifyValues(enabled, val);
    }

    private void updateFlashOnNotifyValues(boolean enabled, String val) {
        if (enabled) {
            if (val.equals("1")) {
                mFlashOnNotifyTimes.setValue(2);
                mFlashOnNotifyRate.setValue(2);
            } else {
                String[] vals = val.split(",");
                mFlashOnNotifyTimes.setValue(Integer.valueOf(vals[0]));
                mFlashOnNotifyRate.setValue(Integer.valueOf(vals[1]));
            }
        }
        mFlashOnNotifyTimes.setVisible(enabled);
        mFlashOnNotifyRate.setVisible(enabled);
    }

    private void setFlashOnNotifyValues(int times, int rate) {
        final boolean enabled = times != 0 && rate != 0;
        String val = String.valueOf(times) + "," + String.valueOf(rate);
        if (times == 2 && rate == 2) val = "1";
        Settings.System.putStringForUser(getActivity().getContentResolver(),
                PREF_FLASH_ON_NOTIFY, enabled ? val : null, UserHandle.USER_CURRENT);
        updateFlashOnNotifyValues(enabled, val);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.EVOLVER;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.evolution_settings_notifications);
}
