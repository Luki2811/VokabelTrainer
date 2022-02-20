package de.luki2811.dev.vokabeltrainer.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Language
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewLessonBinding
import org.json.JSONObject
import java.io.File

class CreateNewLessonFragment : Fragment() {

    private var _binding: FragmentNewLessonBinding? = null
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<String>
    private var arrayListGroup = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewLessonBinding.inflate(inflater, container, false)

        binding.buttonNext.setOnClickListener { createNewLesson() }
        binding.buttonNext.setText(R.string.to_finish)

        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(requireContext()))
        arrayList = ArrayList()
        for(i in 0 until index.getJSONArray("index").length()) {
           arrayList.add(index.getJSONArray("index").getJSONObject(i).getString("name"))
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.default_list_item, arrayList.toTypedArray())
        binding.autoCompleteTextVocabularyGroups.setAdapter(adapter)


        binding.buttonAddVocabularyGroupToLesson.setOnClickListener {
            for (i in arrayList){
                if(binding.autoCompleteTextVocabularyGroups.text.toString() == i && !arrayListGroup.contains(binding.autoCompleteTextVocabularyGroups.text.toString())){
                    addChiptoGroup(i)
                    arrayListGroup.add(i)
                    binding.autoCompleteTextVocabularyGroups.text = null
                }
            }
        }

        binding.chipKnownLan0.text = Language(0, requireContext()).name
        binding.chipKnownLan1.text = Language(1, requireContext()).name
        binding.chipKnownLan2.text = Language(2, requireContext()).name
        binding.chipKnownLan3.text = Language(3, requireContext()).name
        binding.chipKnownLan4.text = Language(4, requireContext()).name
        binding.chipKnownLan5.text = Language(5, requireContext()).name
        binding.chipKnownLan6.text = Language(6, requireContext()).name
        binding.chipKnownLan7.text = Language(7, requireContext()).name
        binding.chipKnownLan8.text = Language(8, requireContext()).name
        binding.chipKnownLan9.text = Language(9, requireContext()).name

        binding.chipNewLan0.text = Language(0, requireContext()).name
        binding.chipNewLan1.text = Language(1, requireContext()).name
        binding.chipNewLan2.text = Language(2, requireContext()).name
        binding.chipNewLan3.text = Language(3, requireContext()).name
        binding.chipNewLan4.text = Language(4, requireContext()).name
        binding.chipNewLan5.text = Language(5, requireContext()).name
        binding.chipNewLan6.text = Language(6, requireContext()).name
        binding.chipNewLan7.text = Language(7, requireContext()).name
        binding.chipNewLan8.text = Language(8, requireContext()).name
        binding.chipNewLan9.text = Language(9, requireContext()).name



        return binding.root
    }

    private fun addChiptoGroup(groupName: String){
        val chip = Chip(requireContext())
        chip.text = groupName
        chip.chipIcon = ContextCompat.getDrawable(requireContext(),
            R.drawable.ic_launcher_background
        )
        chip.isChipIconVisible = false
        chip.isCloseIconVisible = true
        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        binding.chipGroupSelectedVocabularyGroups.addView(chip as View)
        chip.setOnCloseIconClickListener {
            arrayListGroup.remove(groupName)
            binding.chipGroupSelectedVocabularyGroups.removeView(chip as View)
        }
    }
    private fun createNewLesson(){

       val name: String = when(Lesson.isNameValid(requireContext(), binding.textLessonName)){
            0 -> {
                binding.textLessonName.error = null
                binding.textLessonName.text.toString()
            }
            1 -> {
                binding.textLessonName.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
                binding.textLessonName.error = getString(R.string.err_name_already_taken)
                return
            }
            3 -> {
                binding.textLessonName.error = getString(R.string.err_name_too_long_max, 50)
                return
            }
            4 -> {
                binding.textLessonName.error = getString(R.string.err_missing_name)
                return
            }
           else -> {
               Toast.makeText(requireContext(), getString(R.string.err), Toast.LENGTH_SHORT).show()
               return
           }
       }

        val langKnown = when(binding.chipGroupKnownLan.checkedChipId){
            binding.chipKnownLan0.id -> Language(0, requireContext())
            binding.chipKnownLan1.id -> Language(1, requireContext())
            binding.chipKnownLan2.id -> Language(2, requireContext())
            binding.chipKnownLan3.id -> Language(3, requireContext())
            binding.chipKnownLan4.id -> Language(4, requireContext())
            binding.chipKnownLan5.id -> Language(5, requireContext())
            binding.chipKnownLan6.id -> Language(6, requireContext())
            binding.chipKnownLan7.id -> Language(7, requireContext())
            binding.chipKnownLan8.id -> Language(8, requireContext())
            binding.chipKnownLan9.id -> Language(9, requireContext())

            else -> {
                Toast.makeText(requireContext(), getString(R.string.err), Toast.LENGTH_SHORT).show()
                return
            }
        }

        val langNew: Language = when(binding.chipGroupNewLan.checkedChipId){
            binding.chipNewLan0.id -> Language(0, requireContext())
            binding.chipNewLan1.id -> Language(1, requireContext())
            binding.chipNewLan2.id -> Language(2, requireContext())
            binding.chipNewLan3.id -> Language(3, requireContext())
            binding.chipNewLan4.id -> Language(4, requireContext())
            binding.chipNewLan5.id -> Language(5, requireContext())
            binding.chipNewLan6.id -> Language(6, requireContext())
            binding.chipNewLan7.id -> Language(7, requireContext())
            binding.chipNewLan8.id -> Language(8, requireContext())
            binding.chipNewLan9.id -> Language(9, requireContext())
            else -> {
                Toast.makeText(requireContext(), getString(R.string.err), Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Settings

        // If selected is it false
        val settingReadOutBoth: Boolean = !binding.switchLessonSettingsReadOutBoth.isChecked
        val settingAskOnlyNewWords: Boolean = binding.switchLessonSettingsAskOnlyNewWords.isChecked





        val vocabularyGroupsIds: ArrayList<Int> = ArrayList()

        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(requireContext()))

        if(arrayListGroup.isEmpty()){
            Toast.makeText(requireContext(), "Vokabelgruppe fehlt", Toast.LENGTH_SHORT).show()
            return
        }

        val typesOfLesson: ArrayList<Int> = arrayListOf()
        if(binding.chipTypeLesson1.isChecked)
            typesOfLesson.add(1)
        if(binding.chipTypeLesson2.isChecked)
            typesOfLesson.add(2)
        if(binding.chipTypeLesson3.isChecked)
            typesOfLesson.add(3)

        if(typesOfLesson.isEmpty()){
            Toast.makeText(requireContext(), R.string.err_need_one_type_of_lesson, Toast.LENGTH_LONG).show()
            return
        }


        for(i in 0 until index.getJSONArray("index").length()) {
            if(arrayListGroup.contains(index.getJSONArray("index").getJSONObject(i).getString("name"))){
                vocabularyGroupsIds.add(index.getJSONArray("index").getJSONObject(i).getInt("id"))
            }
        }

        val lesson = Lesson(name, langKnown, langNew, vocabularyGroupsIds.toTypedArray(), requireContext(), settingReadOutBoth, settingAskOnlyNewWords, typesOfLesson)

        lesson.saveInIndex()
        lesson.saveInFile()

        startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

    }


    /** private fun goNext() {

        val lesson = Lesson(binding.textLessonName.text.toString())

        // Check Name
        when (Lesson.isNameValid(requireContext(), binding.textLessonName)) {
            0 -> binding.textLessonName.error = null
            1 -> {
                binding.textLessonName.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
                binding.textLessonName.error = getString(R.string.err_name_already_taken)
                return
            }
            3 -> {
                binding.textLessonName.error = getString(R.string.err_name_too_long_max, 50)
                return
            }
            4 -> {
                binding.textLessonName.error = getString(R.string.err_missing_name)
                return
            }
        }
        // Add Language Type to lesson
        lesson.languageNew =
            when(binding.chipGroupNewLan.checkedChipId){
                binding.chipEnglishNewLan.id -> Language(Language.ENGLISH)
                binding.chipGermanNewLan.id -> Language(Language.GERMAN)
                binding.chipSwedishNewLan.id -> Language(Language.SWEDISH)
                binding.chipFrenchNewLan.id -> Language(Language.FRENCH)
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.err_no_new_selected),Toast.LENGTH_SHORT).show()
                    return
                }
        }
        lesson.languageKnow =
            when(binding.chipGroupNativeLan.checkedChipId){
                binding.chipEnglishNativeLan.id -> Language(Language.ENGLISH)
                binding.chipGermanNativeLan.id -> Language(Language.GERMAN)
                binding.chipSwedishNativeLan.id -> Language(Language.SWEDISH)
                binding.chipFrenchNativeLan.id -> Language(Language.FRENCH)
                else -> {
                    Toast.makeText(requireContext(), getString(R.string.err_no_new_selected),Toast.LENGTH_SHORT).show()
                    return
                }
            }

        Toast.makeText(requireContext(), getString(R.string.s_continue), Toast.LENGTH_SHORT).show()

        val dataBundle = bundleOf("key_lesson" to lesson)


    }

    fun checkAndGoNext(view: View) {
    val textName = findViewById<EditText>(R.id.TextLektionName)
    val en_native = findViewById<RadioButton>(R.id.radioButton_native_english)
    val de_native = findViewById<RadioButton>(R.id.radioButton_native_german)
    val sv_native = findViewById<RadioButton>(R.id.radioButton_native_swedish)
    val fr_native = findViewById<RadioButton>(R.id.radioButton_native_french)
    val en_new = findViewById<RadioButton>(R.id.radioButton_new_english)
    val de_new = findViewById<RadioButton>(R.id.radioButton_new_german)
    val sv_new = findViewById<RadioButton>(R.id.radioButton_new_swedish)
    val fr_new = findViewById<RadioButton>(R.id.radioButton_new_french)
    if (textName.text == null || textName.text.toString().trim { it <= ' ' }.isEmpty()) {
    Toast.makeText(this, getString(R.string.err_missing_name), Toast.LENGTH_SHORT).show()
    } else {
    val indexFile = File(applicationContext.filesDir, AppFile.NAME_FILE_INDEX)
    val indexDatei = AppFile(AppFile.NAME_FILE_INDEX)
    if (indexFile.exists()) {
    if (textName.text.toString().trim { it <= ' ' }.contains("/") ||
    textName.text.toString().trim { it <= ' ' }.contains("<") ||
    textName.text.toString().trim { it <= ' ' }.contains(">") ||
    textName.text.toString().trim { it <= ' ' }.contains("\\") ||
    textName.text.toString().trim { it <= ' ' }.contains("|") ||
    textName.text.toString().trim { it <= ' ' }.contains("*") ||
    textName.text.toString().trim { it <= ' ' }.contains(":") ||
    textName.text.toString().trim { it <= ' ' }.contains("\"") ||
    textName.text.toString().trim { it <= ' ' }.contains("?")
    ) {
    Toast.makeText(
    this,
    getString(R.string.err_name_contains_wrong_letter),
    Toast.LENGTH_SHORT
    ).show()
    return
    }
    try {
    val indexJson = JSONObject(indexDatei.loadFromFile(this))
    val indexArray = indexJson.getJSONArray("index")
    for (i in 0 until indexArray.length()) {
    if (indexArray.getJSONObject(i)
    .getString("name") == textName.text.toString()
    .trim { it <= ' ' } || textName.text.toString().trim { it <= ' ' }
    .equals("streak", ignoreCase = true)
    || textName.text.toString().trim { it <= ' ' }
    .equals("settings", ignoreCase = true)
    || textName.text.toString().trim { it <= ' ' }
    .equals("indexLections", ignoreCase = true)
    ) {
    Toast.makeText(
    this,
    getString(R.string.err_name_already_taken),
    Toast.LENGTH_SHORT
    ).show()
    return
    }
    }
    } catch (e: JSONException) {
    e.printStackTrace()
    }
    }
    val JSONFile = JSONObject()
    try {
    // Einstellungen der Lektion als .json
    // Name
    JSONFile.put("name", textName.text.toString())
    // Setzen der Variable für "count"
    JSONFile.put("count", 0)
    // Type Native Sprache
    if (en_native.isChecked) JSONFile.put(
    "languageNative",
    Language.ENGLISH
    ) else if (de_native.isChecked) JSONFile.put(
    "languageNative",
    Language.GERMAN
    ) else if (fr_native.isChecked) JSONFile.put(
    "languageNative",
    Language.FRENCH
    ) else if (sv_native.isChecked) JSONFile.put(
    "languageNative",
    Language.SWEDISH
    ) else Toast.makeText(
    this,
    getString(R.string.err_no_native_selected),
    Toast.LENGTH_LONG
    ).show()
    // Type neue Sprache
    if (en_new.isChecked) JSONFile.put(
    "languageNew",
    Language.ENGLISH
    ) else if (de_new.isChecked) JSONFile.put(
    "languageNew",
    Language.GERMAN
    ) else if (fr_new.isChecked) JSONFile.put(
    "languageNew",
    Language.FRENCH
    ) else if (sv_new.isChecked) JSONFile.put(
    "languageNew",
    Language.SWEDISH
    ) else Toast.makeText(
    this,
    getString(R.string.err_no_new_selected),
    Toast.LENGTH_LONG
    ).show()
    } catch (e: JSONException) {
    e.printStackTrace()
    }
    val intent = Intent(this, CreateNewVocabularyActivity::class.java)
    intent.putExtra(CreateNewMainFragment.JSON_OBJECT, JSONFile.toString())
    startActivity(intent)
    }
    }**/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}