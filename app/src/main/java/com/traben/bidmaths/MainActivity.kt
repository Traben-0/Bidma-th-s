package com.traben.bidmaths

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.traben.bidmaths.databinding.ActivityMainBinding
import com.traben.bidmaths.screens.PREFERENCES
import com.traben.bidmaths.screens.SettingsFragment


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //allows out of context string resource getting
        getStringInvoker = {getString(it)}

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialise the static variables setup in SettingsFragment's companion for ease of use
        SettingsFragment.initSettings(getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE))

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.settings_fragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    // this method was sourced from stack overflow
    // simply detects the display orientation
    fun isLandscape(context: Context): Boolean {
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        @Suppress("DEPRECATION") val display: Display = windowManager.defaultDisplay
        val rotation: Int = display.rotation
        val isPortrait = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180
        return !isPortrait
    }

    companion object{

        private var getStringInvoker : (Int) -> String = {it.toString()}
        fun getString(key:Int) : String{
            return getStringInvoker.invoke(key)
        }
    }
}