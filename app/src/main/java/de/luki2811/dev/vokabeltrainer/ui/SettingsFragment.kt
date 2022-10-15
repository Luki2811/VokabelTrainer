package de.luki2811.dev.vokabeltrainer.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSettingsBinding
import io.github.g0dkar.qrcode.ErrorCorrectionLevel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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

        binding.buttonSettingsInfoQrCodeCorrectionLevel.apply {
                setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = if(settings.appLanguage == Locale.GERMAN)
                            Uri.parse("https://de.wikipedia.org/wiki/QR-Code#Kapazit%C3%A4t_und_Fehlertoleranz")
                        else
                            Uri.parse("https://en.wikipedia.org/wiki/QR_code#Error_correction")
                    }
                    try {
                        startActivity(intent)
                    }catch (e: ActivityNotFoundException){
                        e.printStackTrace()
                        Toast.makeText(requireContext(), getText(R.string.err_no_webbrowser), Toast.LENGTH_LONG).show()
                    }

                }
        }

        // Setup Views
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, Streak.getStreakGoals())
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

        binding.switchSettingsEnableSuggestionsEditor.apply {
            isChecked = settings.suggestTranslation
            setOnCheckedChangeListener { _, isChecked ->
                settings.suggestTranslation = isChecked
                saveSettings()
            }
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

        val adapterLanguages = ArrayAdapter(
            requireContext(),
            R.layout.list_item_default,
            arrayOf(Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH), Locale.GERMANY.getDisplayLanguage(Locale.GERMANY))
        )

        binding.menuSettingsAppLanguage.apply {
            setText(settings.appLanguage.getDisplayLanguage(settings.appLanguage), false)
            setAdapter(adapterLanguages)
            setOnItemClickListener { _, _, _, _ ->
                settings.appLanguage = when(binding.menuSettingsAppLanguage.text.toString()){
                    Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH) -> Locale.ENGLISH
                    Locale.GERMAN.getDisplayLanguage(Locale.GERMAN) -> Locale.GERMAN
                    else -> Locale.ENGLISH
                }
                saveSettings()
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(settings.appLanguage.language))
            }
        }


        binding.menuSettingsQrCodeCorrectionLevel.apply {
            val tempErrorCorrectionLevel = when(settings.correctionLevelQrCode.value){
                ErrorCorrectionLevel.M.value -> "M"
                ErrorCorrectionLevel.H.value -> "H"
                ErrorCorrectionLevel.L.value -> "L"
                ErrorCorrectionLevel.Q.value -> "Q"
                else -> "M"
            }
            setText(tempErrorCorrectionLevel,false)
            setAdapter(ArrayAdapter(requireContext(), R.layout.list_item_default, arrayOf("L","M","Q","H")))
            setOnItemClickListener { _, _, _, _ ->
                settings.correctionLevelQrCode = when(this.text.toString()){
                    "L" -> ErrorCorrectionLevel.L
                    "M" -> ErrorCorrectionLevel.M
                    "Q" -> ErrorCorrectionLevel.Q
                    "H" -> ErrorCorrectionLevel.H
                    else -> ErrorCorrectionLevel.M
                }
                saveSettings()
            }
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
                requestNotificationPermission()
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
        val streakNotificationTag = "streakNotification"

        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(streakNotificationTag)
        var durationDelay = Duration.between(LocalTime.now(), LocalTime.of(settings.timeReminderStreak.hour, settings.timeReminderStreak.minute))

        if(durationDelay.isNegative || durationDelay.isZero){
            durationDelay = Duration.between(
                LocalDateTime.now(),
                LocalDateTime.of(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(
                        settings.timeReminderStreak.hour, settings.timeReminderStreak.minute
                    )
                )
            )
        }

        val notificationWorkerRequest: WorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>().apply {
            addTag(streakNotificationTag)
            setInputData(Data.Builder().putBoolean("oneTimeRequest", true).build())
            setInitialDelay(durationDelay)
        }.build()


        WorkManager.getInstance(requireContext()).enqueue(notificationWorkerRequest)

        Log.i("StreakNotification","Setup streak")
    }

    private fun pickTimeStreak() {
        val picker = MaterialTimePicker.Builder()
            .setHour(settings.timeReminderStreak.hour)
            .setMinute(settings.timeReminderStreak.minute)
            .setTimeFormat(if(is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setTitleText(getString(R.string.when_do_you_want_to_be_remembered))
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()

        picker.addOnPositiveButtonClickListener {
            settings.timeReminderStreak = LocalTime.of(picker.hour, picker.minute)
            saveSettings()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestNotificationPermission()
            }else{
                setupNotifications()
            }
        }

        picker.show(parentFragmentManager, "timePickerStreak")
    }

    @RequiresApi(33)
    private fun requestNotificationPermission(){
        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun saveSettings(){
        settings.saveSettingsInFile()
    }

    override fun onResume() {
        super.onResume()

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_default, Streak.getStreakGoals())
        binding.menuStreakDailyObjectiveXPAutoComplete.setAdapter(adapter)

        val adapterLanguages = ArrayAdapter(requireContext(), R.layout.list_item_default, arrayOf(Locale.ENGLISH.getDisplayLanguage(Locale.ENGLISH), Locale.GERMAN.getDisplayLanguage(Locale.GERMAN)))
        binding.menuSettingsAppLanguage.apply {
            setAdapter(adapterLanguages)
            setText(settings.appLanguage.getDisplayLanguage(settings.appLanguage), false)
        }

        binding.menuSettingsQrCodeCorrectionLevel.apply {
            setAdapter(ArrayAdapter(requireContext(), R.layout.list_item_default, arrayOf("L","M","Q","H")))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}