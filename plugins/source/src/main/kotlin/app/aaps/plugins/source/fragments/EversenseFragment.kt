package app.aaps.plugins.source.fragments

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import app.aaps.plugins.source.R

class EversenseFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.eversense_preferences, rootKey)
        setupListeners()
    }

    private fun setupListeners() {
        findPreference<Preference>("eversense_calibrate")?.setOnPreferenceClickListener {
            // Replaced ToastUtils with standard Toast to fix error
            Toast.makeText(context, "Opening Calibration Interface...", Toast.LENGTH_SHORT).show()
            true
        }

        findPreference<Preference>("eversense_connect")?.setOnPreferenceClickListener {
            Toast.makeText(context, "Scanning for Smart Transmitter...", Toast.LENGTH_SHORT).show()
            true
        }
    }
}