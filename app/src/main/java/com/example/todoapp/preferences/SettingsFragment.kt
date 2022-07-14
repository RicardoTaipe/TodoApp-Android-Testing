package com.example.todoapp.preferences

import android.app.UiModeManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.todoapp.R

const val DARK_MODE_KEY = "dark_mode"

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val darkModeSwitch = findPreference<SwitchPreferenceCompat>(DARK_MODE_KEY)
        darkModeSwitch?.setOnPreferenceChangeListener { _, newValue ->
            AppCompatDelegate.setDefaultNightMode(
                if (newValue as Boolean) UiModeManager.MODE_NIGHT_YES else UiModeManager.MODE_NIGHT_NO
            )
            true
        }
    }
}