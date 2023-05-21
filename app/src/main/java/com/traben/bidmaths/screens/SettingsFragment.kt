package com.traben.bidmaths.screens

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
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.room.Room
import com.traben.bidmaths.LeaderBoard
import com.traben.bidmaths.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


const val PREFERENCES = "bidmaths_settings"
const val LEFT_TO_RIGHT_KEY = "respect_left_to_right"
const val HINT_KEY = "enable_hints"
const val HIDE_LEADERBOARD_KEY = "leader_board_hidden"

/**
 * A settings fragments generated thanks to PreferenceFragmentCompat()
 * this provides a very simple yet formal looking settings screen
 * this conflict in style is by design as the settings screen is never intended to be seen or used
 * by children, which is what the rest of the fragments are stylistically designed around
 *
 * */

class SettingsFragment : PreferenceFragmentCompat() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // retrieve the existing SharedPreferences instance
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        // set the existing SharedPreferences instance to the preference manager
        preferenceManager.sharedPreferencesName = PREFERENCES
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //this hides the options button on the toolbar
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        @Suppress("DEPRECATION")
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //adds functionality to the reset leaderboard button
        // it isn't technically a button but it works
        val buttonPreference: Preference? = findPreference("reset_leaderboard")
        buttonPreference?.setOnPreferenceClickListener {
            //display a generic confirmation box
            displayConfirmationDialogBox(
                getString(R.string.delete_leaderboard_sure),
                { dialog, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        clearDatabase()
                    }
                    Toast.makeText(requireContext(), getString(R.string.delete_leaderboard_success),
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                },
                { dialog, _ ->
                    dialog.dismiss()
                }, requireContext()
            )
            true
        }
    }

    //clear the database of all values
    private fun clearDatabase() {
        val database =
            Room.databaseBuilder(requireContext(), LeaderBoard::class.java, "leader-board")
                .build()
        database.getDao().clearAllEntries()
        database.close()
    }

    // the intention of this method is be general use for any future settings
    // it is only used once currently hence the same parameter value suppression
    private fun displayConfirmationDialogBox(
        @Suppress("SameParameterValue") message: String,
        yes: DialogInterface.OnClickListener,
        no: DialogInterface.OnClickListener,
        context: Context
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmation")
            .setMessage(message)
            .setPositiveButton("Yes", yes)
            .setNegativeButton("No", no)
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

    //update the companions static booleans
    private fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            LEFT_TO_RIGHT_KEY ->    respectLeftRight= sharedPreferences.getBoolean(key, true)
            HINT_KEY ->             hintsEnabled    = sharedPreferences.getBoolean(key, true)
            HIDE_LEADERBOARD_KEY -> hideLeaderboard = sharedPreferences.getBoolean(key, false)
        }
    }


    companion object {
        //the companion object houses these static booleans to simplify usage of settings elsewhere
        var respectLeftRight = true
        var hintsEnabled = true
        var hideLeaderboard = false

        //called on MainActivity creation
        fun initSettings(sharedPreferences: SharedPreferences?) {
            respectLeftRight = sharedPreferences?.getBoolean(LEFT_TO_RIGHT_KEY, true) ?: true
            hintsEnabled = sharedPreferences?.getBoolean(HINT_KEY, true) ?: true
            hideLeaderboard = sharedPreferences?.getBoolean(HIDE_LEADERBOARD_KEY, false) ?: false
        }

    }
}