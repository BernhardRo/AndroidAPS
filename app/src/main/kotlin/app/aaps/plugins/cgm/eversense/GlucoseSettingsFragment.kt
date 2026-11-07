package app.aaps.plugins.cgm.eversense

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import app.aaps.plugins.source.R

class GlucoseSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.eversense_glucose_settings, rootKey)
    }
}