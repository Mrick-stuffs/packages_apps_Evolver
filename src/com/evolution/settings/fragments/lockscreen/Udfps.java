/*
 * Copyright (C) 2017-2022 The Project-Xtended
 *               2023 Evolution X
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
package com.evolution.settings.fragments.lockscreen;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.evolution.EvolutionUtils;
import com.evolution.settings.fragments.lockscreen.UdfpsIconPicker;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class Udfps extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Udfps";

    private static final String UDFPS_CUSTOMIZATION = "udfps_customization";
    private static final String KEY_UDFPS_ICONS = "udfps_icon_picker";

    private PreferenceCategory mUdfpsCustomization;
    private Preference mUdfpsIcons;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.evolution_settings_udfps;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        Resources resources = getResources();

        final boolean udfpsResPkgInstalled = EvolutionUtils.isPackageInstalled(getContext(),
                "com.evolution.udfps.resources");
        mUdfpsCustomization = (PreferenceCategory) findPreference(UDFPS_CUSTOMIZATION);
        if (!udfpsResPkgInstalled) {
            prefScreen.removePreference(mUdfpsCustomization);
        }

         final boolean udfpsResIconPkgInstalled = EvolutionUtils.isPackageInstalled(getContext(),
                "com.evolution.udfps.icons");
        mUdfpsIcons = (Preference) findPreference(KEY_UDFPS_ICONS);
        if (!udfpsResIconPkgInstalled) {
            prefScreen.removePreference(mUdfpsIcons);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        return false;
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
            new BaseSearchIndexProvider(R.xml.evolution_settings_udfps);
}
