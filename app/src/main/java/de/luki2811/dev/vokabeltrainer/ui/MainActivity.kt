package de.luki2811.dev.vokabeltrainer.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.scheme == ContentResolver.SCHEME_CONTENT && intent.action == Intent.ACTION_VIEW){
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.action_import)
                .setMessage(getString(R.string.q_start_import, intent.data?.lastPathSegment))
                .setIcon(R.drawable.ic_baseline_import_export_24)
                .setPositiveButton(R.string.yes){ _, _ ->
                    val uri = intent.data
                    intent.action = null
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
                    navHostFragment.findNavController().navigate(MobileNavigationDirections.actionGlobalCreateNewMainFragment(uri))
                }
                .setNegativeButton(R.string.cancel){_, _ ->
                    intent.action = null
                }
                .setOnCancelListener {
                    intent.action = null
                }.show()
        }

        val settings = Settings(this)
        val systemLang = Locale(AppCompatDelegate.getApplicationLocales().toLanguageTags().replaceAfter('-',"").replace("-","").trim())

        if(systemLang != settings.appLanguage){
            settings.appLanguage = when(systemLang){
                Locale.GERMAN -> Locale.GERMAN
                Locale.ENGLISH -> Locale.ENGLISH
                else -> Locale.ENGLISH
            }
            settings.saveSettingsInFile()
        }

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.appLanguage.language))

        createFiles()
        setupViews()

        // Load Streak for correct information
        Streak(applicationContext)

        /** TEMP to delete a wrong ID without a file
        // vocabulary group
        // val tempVocGroup = VocabularyGroup("", Language(0, applicationContext), Language(0, applicationContext), arrayOf(), applicationContext)
        // tempVocGroup.id = Id(applicationContext, 498725)
        // tempVocGroup.deleteFromIndex()

        // lesson
        // val tempLesson = Lesson("", arrayOf(), applicationContext)
        // tempLesson.id = Id(applicationContext, 217531)
        // tempLesson.deleteFromIndex() **/
    }

    /**
     * Create files which did not exist before
     */
    private fun createFiles() {
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

        if (!File(applicationContext.filesDir, AppFile.NAME_FILE_STREAK).exists()) {
            val streakData = Streak.getRandomStreak(0)
            AppFile.writeInFile(streakData.toString(), File(applicationContext.filesDir, AppFile.NAME_FILE_STREAK))
        }
    }

    /**
     * Setup views
     */
    private fun setupViews() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        binding.floatingActionButtonAdd.setOnClickListener {
            navController.navigate(R.id.action_global_createNewMainFragment)
        }

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility = if(destination.id == R.id.learnFragment || destination.id == R.id.streakFragment || destination.id == R.id.settingsFragment) View.VISIBLE else View.GONE
            binding.floatingActionButtonAdd.visibility = if(destination.id == R.id.learnFragment || destination.id == R.id.settingsFragment) View.VISIBLE else View.GONE
        }
    }

    override fun onStop() {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = StreakWidget.getUpdatedViews(applicationContext)
        val componentName = ComponentName(application, StreakWidget::class.java)
        val ids: IntArray = AppWidgetManager.getInstance(application).getAppWidgetIds(componentName)

        val intent = Intent(this, StreakWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }

        appWidgetManager.updateAppWidget(componentName, remoteViews)
        sendBroadcast(intent)
        super.onStop()
    }
}