package de.luki2811.dev.vokabeltrainer.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.ActivityMainBinding
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createFiles()

        setupViews()

        // Load Streak for correct information
        Streak(applicationContext)

        /** TEMP to delete a wrong ID without a file
        // vocabulary group
        // val vocg = VocabularyGroup("", Language(0, applicationContext), Language(0, applicationContext), arrayOf(), applicationContext)
        // vocg.id = Id(applicationContext, 498725)
        // vocg.deleteFromIndex()

        // lesson
        // val lessong = Lesson("", arrayOf(), applicationContext)
        // lessong.id = Id(applicationContext, 217531)
        // lessong.deleteFromIndex() **/
    }

    private fun createFiles() {
        // Erstellen der IndexDatein mit leerem Index
        val indexVocGroupsFile = File(this.filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if (!indexVocGroupsFile.exists())
            AppFile.writeInFile(
                JSONObject().put("index", JSONArray()).toString(),
                indexVocGroupsFile
            )

        if (!File(this.filesDir, AppFile.NAME_FILE_SETTINGS).exists())
            Settings(this).saveSettingsInFile()

        val wrongWordsFile = File(this.filesDir, AppFile.NAME_FILE_LIST_WRONG_WORDS)
        if (!wrongWordsFile.exists())
            AppFile.writeInFile("[]", wrongWordsFile)

        val indexIdFile = File(this.filesDir, AppFile.NAME_FILE_INDEX_ID)
        if (!indexIdFile.exists())
            AppFile.writeInFile(JSONObject().put("index", JSONArray()).toString(), indexIdFile)

        val indexLessonFile = File(this.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        if (!indexLessonFile.exists())
            AppFile.writeInFile(JSONObject().put("index", JSONArray()).toString(), indexLessonFile)

        val indexLanguageFile = File(this.filesDir, AppFile.NAME_FILE_INDEX_LANGUAGES)
        if (!indexLanguageFile.exists())
            // AppFile.writeInFile(Language.getDefaultLanguageIndex().toString(), indexLanguageFile)

        if (!File(applicationContext.filesDir, AppFile.NAME_FILE_STREAK).exists()) {
            AppFile.writeInFile("[]", File(applicationContext.filesDir, AppFile.NAME_FILE_STREAK))
            val streakData = JSONArray().put(
                JSONObject().put(
                    "date",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ).put("xp", 0).put("goal", 50)
            )
            AppFile.writeInFile(
                streakData.toString(),
                File(applicationContext.filesDir, AppFile.NAME_FILE_STREAK)
            )
        }
    }

    private fun setupViews() {
        // TODO: TopBar Namen berarbeiten (Label ändern der Fragmente)

        val navView: BottomNavigationView = binding.bottomNavigation

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.floatingActionButton.setOnClickListener {
            navController.navigate(R.id.action_global_createNewMainFragment)
        }
        // binding.floatingActionButtonQrCode.setOnClickListener {
        // navController.navigate(R.id.action_global_importWithQrCodeFragment)
        // }
        binding.floatingActionButtonPractice.setOnClickListener {
            startActivity(Intent(this, PracticeActivity::class.java))
            // navController.navigate(R.id.action_global_createPracticeFragment)
        }

        navView.setupWithNavController(navController)

        // val appBarConfiguration = AppBarConfiguration(setOf(R.id.learnFragment, R.id.streakFragment, R.id.settingsFragment))
        // setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.learnFragment && destination.id != R.id.settingsFragment && destination.id != R.id.streakFragment) {
                navView.visibility = View.GONE
                // binding.floatingActionButtonQrCode.visibility = View.GONE
                binding.floatingActionButton.visibility = View.GONE
            } else {
                navView.visibility = View.VISIBLE
                binding.floatingActionButton.visibility = View.VISIBLE
                // binding.floatingActionButtonQrCode.visibility = View.VISIBLE
            }

            if (destination.id == R.id.learnFragment) {
                binding.floatingActionButtonPractice.visibility = View.VISIBLE
            } else {
                binding.floatingActionButtonPractice.visibility = View.GONE
            }
        }
    }
}