package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.luki2811.dev.vokabeltrainer.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
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
    }

    private fun setupViews() {
        // TODO: TopBar Namen berarbeiten

        val navView: BottomNavigationView = binding.bottomNavigation

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.learnFragment, R.id.streakFragment, R.id.settingsFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

    }


    fun createNewButtonOnClick(view: View?) {
        startActivity(Intent(applicationContext, CreateNewActivity::class.java))
    }

    companion object {
        const val LEKTION_NAME = "de.luki2811.dev.vokabeltrainer"
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