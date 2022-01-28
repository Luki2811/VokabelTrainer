package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivitiesIfAvailable(application)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()

        // Erstellen der IndexDatein mit leerem Index
        if(!File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).exists())
            AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).writeInFile(JSONObject().put("index", JSONArray()).toString(), applicationContext)

        if(!File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX_ID).exists())
            AppFile(AppFile.NAME_FILE_INDEX_ID).writeInFile(JSONObject().put("index", JSONArray()).toString(), applicationContext)

        if(!File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX_LESSONS).exists())
            AppFile(AppFile.NAME_FILE_INDEX_LESSONS).writeInFile(JSONObject().put("index", JSONArray()).toString(), applicationContext)

        if(!File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES).exists())
            AppFile(AppFile.NAME_FILE_INDEX_LANGUAGES).writeInFile(Language.getDefaultLanguageIndex().toString(), applicationContext)
    }

    private fun setupViews() {
        // TODO: TopBar Namen berarbeiten (Label ändern der Fragmente)

        val navView: BottomNavigationView = binding.bottomNavigation

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.floatingActionButton.setOnClickListener { navController.navigate(R.id.action_global_navigation_create) }

        navView.setupWithNavController(navController)

        // val appBarConfiguration = AppBarConfiguration(setOf(R.id.learnFragment, R.id.streakFragment, R.id.settingsFragment))
        // setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if( destination.id != R.id.learnFragment && destination.id != R.id.settingsFragment && destination.id != R.id.streakFragment) {
                navView.visibility = View.GONE
                binding.floatingActionButton.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
                binding.floatingActionButton.visibility = View.VISIBLE
            }
        }

    }


    companion object {
        /**
         * Rundet den übergebenen Wert auf die Anzahl der übergebenen Nachkommastellen
         *
         * @param value ist der zu rundende Wert.
         * @param decimalPoints ist die Anzahl der Nachkommastellen, auf die gerundet werden soll.
         */
        fun round(value: Double, decimalPoints: Int): Double {
            val d = Math.pow(10.0, decimalPoints.toDouble())
            return Math.round(value * d) / d
        }
    }
}