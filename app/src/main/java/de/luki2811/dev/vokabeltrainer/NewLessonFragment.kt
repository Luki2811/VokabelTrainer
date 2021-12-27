package de.luki2811.dev.vokabeltrainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewLessonBinding
import org.json.JSONObject
import java.io.File

class NewLessonFragment : Fragment() {

    private var _binding: FragmentNewLessonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewLessonBinding.inflate(inflater, container, false)

        binding.buttonNext.setOnClickListener { goNext() }

        return binding.root
    }


    private fun goNext() {

        val lesson = Lesson(binding.textLessonName.text.toString())

        // Check Name
        when (lesson.isNameValid(requireContext(), binding.textLessonName)) {
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

    /** fun checkAndGoNext(view: View) {
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