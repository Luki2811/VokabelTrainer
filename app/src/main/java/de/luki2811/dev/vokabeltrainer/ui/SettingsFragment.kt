package de.luki2811.dev.vokabeltrainer.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSettingsBinding
import java.time.LocalTime
import java.util.*
import kotlin.math.roundToInt

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted)
            setupNotifications()
        else{
            binding.switchSettingsNotificationStreak.isChecked = false
            settings.reminderForStreak = false
            setupNotifications()
            Log.e("Permission","This permission is necessary to send notification")
        }
    }

    // Settings
    private lateinit var settings: Settings

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        settings = Settings(requireContext())

        binding.menuStreakDailyObjectiveXPAutoComplete.setText(getString(R.string.xp, settings.dailyObjectiveStreak), false)
        binding.switchSettingsReadOutVocabularyCentralForbidden.isChecked = !settings.readOutVocabularyGeneral
        binding.switchSettingsIncreaseScreenBrightnessShowingQrCode.isChecked = settings.increaseScreenBrightness

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.switchSettingsEnableDynamicColors.isChecked = settings.useDynamicColors
        }else{
            binding.switchSettingsEnableDynamicColors.isEnabled = false
        }

        binding.textViewSettingsVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME /** BuildConfig.VERSION_CODE **/)

        binding.buttonSettingsManageVocabularyGroups.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToManageVocabularyGroupsFragment())
        }
        binding.buttonSettingsSources.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToSourcesFragment())
        }


        // Setup Views
        val items = arrayListOf("10XP","20XP","30XP","40XP","50XP","60XP","70XP","80XP","90XP","100XP","110XP","120XP","130XP","140XP","150XP","160XP","170XP","180XP","190XP","200XP")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, items)
        binding.menuStreakDailyObjectiveXPAutoComplete.setAdapter(adapter)

        binding.menuStreakDailyObjectiveXPAutoComplete.setOnItemClickListener { _, _, _, _ ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.warning))
                .setIcon(R.drawable.ic_outline_warning_24)
                .setMessage(R.string.warning_lose_of_progress)
                .setPositiveButton(R.string.ok){_, _ ->
                    settings.dailyObjectiveStreak = binding.menuStreakDailyObjectiveXPAutoComplete.text.toString().replace("XP","").toInt()
                    saveSettings()
                }
                .setNegativeButton(R.string.cancel){_, _ ->
                    binding.menuStreakDailyObjectiveXPAutoComplete.setText(getString(R.string.xp, settings.dailyObjectiveStreak), false)
                }
                .setOnCancelListener {
                    binding.menuStreakDailyObjectiveXPAutoComplete.setText(getString(R.string.xp, settings.dailyObjectiveStreak), false)
                }
                .show()
        }

        binding.sliderSettingsStreakChartLength.apply {
            valueFrom = 3f
            value = settings.streakChartLengthInDays.toFloat()
            valueTo = 31f
            stepSize = 1f
            addOnChangeListener { _, value, _ ->
                settings.streakChartLengthInDays = value.roundToInt()
                saveSettings()
            }
        }

        binding.switchSettingsIncreaseScreenBrightnessShowingQrCode.setOnCheckedChangeListener { _, isChecked ->
            settings.increaseScreenBrightness = isChecked
            saveSettings()
        }

        binding.switchSettingsReadOutVocabularyCentralForbidden.setOnCheckedChangeListener { _, isChecked ->
            settings.readOutVocabularyGeneral = !isChecked
            saveSettings()
        }

        binding.switchSettingsEnableDynamicColors.setOnCheckedChangeListener {_, isChecked ->
            settings.useDynamicColors = isChecked
            saveSettings()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.info))
                .setIcon(R.drawable.ic_outline_info_24)
                .setMessage(R.string.info_need_to_restart_app_to_see_change)
                .setPositiveButton(R.string.ok){_, _ ->
                    // requireActivity().recreate()
                }
                .show()
        }

        /** if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.menuSettingsAppLanguageLayout.isEnabled = false
            binding.menuSettingsAppLanguage.isEnabled = false
        } **/

        binding.menuSettingsAppLanguage.setText(settings.appLanguage.getDisplayLanguage(settings.appLanguage), false)

        val adapterLanguages = ArrayAdapter(
            requireContext(),
            R.layout.list_item_default,
            arrayOf(
                Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH),
                Locale.GERMANY.getDisplayLanguage(Locale.GERMANY)
            )
        )

        binding.menuSettingsAppLanguage.setAdapter(adapterLanguages)

        binding.menuSettingsAppLanguage.setOnItemClickListener { _, _, _, _ ->
                settings.appLanguage = when(binding.menuSettingsAppLanguage.text.toString()){
                    Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH) -> Locale.ENGLISH
                    Locale.GERMAN.getDisplayLanguage(Locale.GERMAN) -> Locale.GERMAN
                    else -> Locale.ENGLISH
                }
                saveSettings()
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.appLanguage.language))
            }

        binding.switchSettingsNotificationStreak.isChecked = if(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                settings.reminderForStreak = false
                setupNotifications()
                false
            }else settings.reminderForStreak


        binding.switchSettingsNotificationStreak.setOnCheckedChangeListener { _, isChecked ->
            settings.reminderForStreak = isChecked
            saveSettings()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isChecked){
                requestPermission()
            }else{
                setupNotifications()
            }
        }

        binding.buttonSettingsStreakTime.setOnClickListener { pickTimeStreak() }

        binding.buttonSettingsGoTextToSpeakSettings.setOnClickListener {
            startActivity(Intent("com.android.settings.TTS_SETTINGS"))
        }

        return binding.root
    }

    private fun setupNotifications(){
        try {
            // Setup all for notifications

            val dailyNotify = settings.reminderForStreak
            val pm = requireContext().packageManager
            val receiver = ComponentName(requireContext(), DeviceBootReceiver::class.java)
            val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
            val manager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (dailyNotify) {
                //region Enable Daily Notifications
                val calendar: Calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, settings.timeReminderStreak.hour)
                calendar.set(Calendar.MINUTE, settings.timeReminderStreak.minute)
                calendar.set(Calendar.SECOND, 1)
                // if notification time is before selected time, send notification the next day
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1)
                }
                manager.setRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, pendingIntent
                )
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                //To enable Boot Receiver class
                pm.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                // Toast.makeText(requireContext(),"Streak Notifications were updated", Toast.LENGTH_SHORT).show()
                //endregion
            } else { //Disable Daily Notifications
                if (PendingIntent.getBroadcast(requireContext(), 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE) != null) {
                    manager.cancel(pendingIntent)
                    //Toast.makeText(requireContext(),"Streak Notifications were updated", Toast.LENGTH_SHORT).show()
                }
                pm.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }catch (e: SecurityException){
            e.printStackTrace()
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.err_no_permission_reminder)
                .setTitle(R.string.err)
                .setIcon(R.drawable.ic_outline_error_24)
                .setNegativeButton(R.string.ok){ _, _ -> }
                .setOnCancelListener {  }
                .show()
            settings.reminderForStreak = false
            binding.switchSettingsNotificationStreak.isChecked = false
            saveSettings()
        }

    }

    private fun pickTimeStreak() {
        val picker = MaterialTimePicker.Builder()
            .setHour(settings.timeReminderStreak.hour)
            .setMinute(settings.timeReminderStreak.minute)
            .setTimeFormat(if (is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText(getString(R.string.when_do_you_want_to_be_remembered))
            .build()

        picker.addOnPositiveButtonClickListener {
            settings.timeReminderStreak = LocalTime.of(picker.hour, picker.minute)
            saveSettings()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestPermission()
            }else{
                setupNotifications()
            }
        }

        picker.show(parentFragmentManager, "timePickerStreak")
    }

    @RequiresApi(33)
    private fun requestPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun saveSettings(){
        settings.saveSettingsInFile()
    }

    override fun onResume() {
        super.onResume()
        // settings = Settings(requireContext())
        val items = arrayListOf("10XP","20XP","30XP","40XP","50XP","60XP","70XP","80XP","90XP","100XP","110XP","120XP","130XP","140XP","150XP","160XP","170XP","180XP","190XP","200XP")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, items)
        binding.menuStreakDailyObjectiveXPAutoComplete.setAdapter(adapter)
        val adapterLanguages = ArrayAdapter(
            requireContext(),
            R.layout.list_item_default, arrayOf(
                Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH),
                Locale.GERMAN.getDisplayLanguage(Locale.GERMAN)
            )
        )
        binding.menuSettingsAppLanguage.apply {
            setAdapter(adapterLanguages)
            setText(settings.appLanguage.getDisplayLanguage(settings.appLanguage), false)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}