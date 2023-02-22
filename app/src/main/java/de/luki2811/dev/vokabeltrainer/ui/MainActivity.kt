package de.luki2811.dev.vokabeltrainer.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.FileUtil
import de.luki2811.dev.vokabeltrainer.MobileNavigationDirections
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.ShortForm
import de.luki2811.dev.vokabeltrainer.Streak
import de.luki2811.dev.vokabeltrainer.StreakWidget
import de.luki2811.dev.vokabeltrainer.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.action == "CREATE_NEW")
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment).findNavController().navigate(MobileNavigationDirections.actionGlobalCreateNewMainFragment())

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
            settings.save()
        }

        setupShortcuts()

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.appLanguage.language))

        createFiles()

        /** if(!settings.alreadyShownStart){
            startActivity(Intent(applicationContext, StartActivity::class.java).apply { /** flags = Intent.FLAG_ACTIVITY_CLEAR_TOP **/})
            // TODO settings.alreadyShownStart = false
            settings.saveSettingsInFile()
        } **/
        setupViews()

        // Load Streak for correct information
        Streak(applicationContext)
    }

    private fun setupShortcuts() {
        val shortcut = ShortcutInfoCompat.Builder(this, "id_shortcut_create_new")
            .setShortLabel(this.getString(R.string.msg_shortcut_short_create_new))
            .setLongLabel(this.getString(R.string.msg_shortcut_long_create_new))
            .setRank(1)
            .setIcon(IconCompat.createWithResource(this, R.drawable.ic_outline_add_24))
            .setIntent(Intent(this, MainActivity::class.java).apply {
                action = "CREATE_NEW"
            })
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }

    /**
     * Create files which did not exist before
     */
    private fun createFiles() {
        val indexVocGroupsFile = File(this.filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        if (!indexVocGroupsFile.exists())
            FileUtil.writeInFile(
                JSONObject().put("index", JSONArray()).toString(),
                indexVocGroupsFile
            )

        val shortFormFile = File(this.filesDir, FileUtil.NAME_FILE_SHORT_FORMS)
        if(!shortFormFile.exists()){
            ShortForm.setNewShortForms(applicationContext, ShortForm.getSomeKnownShortForms())
        }

        val wrongWordsFile = File(this.filesDir, FileUtil.NAME_FILE_LIST_WRONG_WORDS)
        if (!wrongWordsFile.exists())
            FileUtil.writeInFile("[]", wrongWordsFile)

        val indexIdFile = File(this.filesDir, FileUtil.NAME_FILE_INDEX_ID)
        if (!indexIdFile.exists())
            FileUtil.writeInFile(JSONObject().put("index", JSONArray()).toString(), indexIdFile)

        val indexLessonFile = File(this.filesDir, FileUtil.NAME_FILE_INDEX_LESSONS)
        if (!indexLessonFile.exists())
            FileUtil.writeInFile(JSONObject().put("index", JSONArray()).toString(), indexLessonFile)

        if (!File(applicationContext.filesDir, FileUtil.NAME_FILE_STREAK).exists()) {
            val streakData = Streak.getRandomStreak(0)
            FileUtil.writeInFile(streakData.toString(), File(applicationContext.filesDir, FileUtil.NAME_FILE_STREAK))
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
            binding.floatingActionButtonAdd.visibility = if(destination.id == R.id.learnFragment) View.VISIBLE else View.GONE
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