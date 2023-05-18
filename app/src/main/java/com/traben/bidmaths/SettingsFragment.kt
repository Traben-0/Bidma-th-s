package com.traben.bidmaths

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


const val PREFERENCES = "bidmaths_settings"
const val LEFT_TO_RIGHT_KEY = "respect_left_to_right"
const val HINT_KEY = "enable_hints"
const val HIDE_RESULT_KEY = "hide_results"

class SettingsFragment : PreferenceFragmentCompat() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Retrieve the existing SharedPreferences instance
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        // Set the existing SharedPreferences instance to the preference manager
        preferenceManager.sharedPreferencesName = PREFERENCES;
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE;

        setPreferencesFromResource(R.xml.root_preferences, rootKey)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonPreference : Preference? = findPreference("reset_leaderboard")
        buttonPreference?.setOnPreferenceClickListener{
            displayConfirmationDialogBox("Are you sure you want to reset the leaderboard?",
                { dialog,_ ->
                    Toast.makeText(requireContext(), "Leaderboard reset.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                { dialog, _ ->
                    dialog.dismiss()
                }, requireContext()
            )
            true
        }
    }

    //useful elsewhere maybe
    public fun displayConfirmationDialogBox(message : String, yes: DialogInterface.OnClickListener, no: DialogInterface.OnClickListener,context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
                .setMessage(message)
                .setPositiveButton("Yes",yes)
                .setNegativeButton("No",no)
            builder.create().show()

    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this::onSharedPreferenceChanged)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this::onSharedPreferenceChanged)
    }

    private fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            LEFT_TO_RIGHT_KEY   ->  respectLeftRight =  sharedPreferences.getBoolean(key, false)
            HINT_KEY            ->  hintsEnabled =      sharedPreferences.getBoolean(key, false)
            HIDE_RESULT_KEY     ->  hideResults =       sharedPreferences.getBoolean(key, false)
        }
    }

    companion object{

        var respectLeftRight = true
        var hintsEnabled = true
        var hideResults = false

        fun initSettings(sharedPreferences: SharedPreferences?) {
            respectLeftRight = sharedPreferences?.getBoolean(LEFT_TO_RIGHT_KEY,true) ?: true
            hintsEnabled = sharedPreferences?.getBoolean(HINT_KEY,true) ?: true
            hideResults = sharedPreferences?.getBoolean(HIDE_RESULT_KEY,false) ?: false
        }

    }
}