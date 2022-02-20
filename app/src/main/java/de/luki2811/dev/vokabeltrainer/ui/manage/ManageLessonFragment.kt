package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewLessonBinding
import org.json.JSONObject

class ManageLessonFragment: Fragment() {
    private var _binding: FragmentNewLessonBinding? = null
    private val binding get() = _binding!!
    private lateinit var arrayList: ArrayList<String>
    private var arrayListOfVocabularyGroupNames = ArrayList<String>()
    private var arrayListGroup = ArrayList<String>()
    private lateinit var lesson: Lesson
    private val args: ManageLessonFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewLessonBinding.inflate(layoutInflater, container, false)
        lesson = Lesson(JSONObject(args.lessonJson), requireContext())

        binding.buttonNext.setOnClickListener { saveLesson() }
        binding.buttonNext.text = getString(R.string.save)

        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(requireContext()))

        arrayList = ArrayList()
        for(i in 0 until index.getJSONArray("index").length()) {
            arrayList.add(index.getJSONArray("index").getJSONObject(i).getString("name"))
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.default_list_item, arrayList.toTypedArray())
        binding.autoCompleteTextVocabularyGroups.setAdapter(adapter)

        for(indexArray in 0 until index.getJSONArray("index").length())
            if(lesson.vocabularyGroupIds.contains(index.getJSONArray("index").getJSONObject(indexArray).getInt("id"))) {
                arrayListOfVocabularyGroupNames.add(index.getJSONArray("index").getJSONObject(indexArray).getString("name"))
            }


        for(i in arrayListOfVocabularyGroupNames){
            addChipToGroup(i)
            arrayListGroup.add(i)
        }

        binding.buttonAddVocabularyGroupToLesson.setOnClickListener {
            for (i in arrayList){
                if(binding.autoCompleteTextVocabularyGroups.text.toString() == i && !arrayListGroup.contains(binding.autoCompleteTextVocabularyGroups.text.toString())){
                    addChipToGroup(i)
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


        when(lesson.languageKnow.name){
            binding.chipKnownLan0.text -> binding.chipKnownLan0.isChecked = true
            binding.chipKnownLan1.text -> binding.chipKnownLan1.isChecked = true
            binding.chipKnownLan2.text -> binding.chipKnownLan2.isChecked = true
            binding.chipKnownLan3.text -> binding.chipKnownLan3.isChecked = true
            binding.chipKnownLan4.text -> binding.chipKnownLan4.isChecked = true
            binding.chipKnownLan5.text -> binding.chipKnownLan5.isChecked = true
            binding.chipKnownLan6.text -> binding.chipKnownLan6.isChecked = true
            binding.chipKnownLan7.text -> binding.chipKnownLan7.isChecked = true
            binding.chipKnownLan8.text -> binding.chipKnownLan8.isChecked = true
            binding.chipKnownLan9.text -> binding.chipKnownLan9.isChecked = true
        }

        when(lesson.languageNew.name){
            binding.chipNewLan0.text -> binding.chipNewLan0.isChecked = true
            binding.chipNewLan1.text -> binding.chipNewLan1.isChecked = true
            binding.chipNewLan2.text -> binding.chipNewLan2.isChecked = true
            binding.chipNewLan3.text -> binding.chipNewLan3.isChecked = true
            binding.chipNewLan4.text -> binding.chipNewLan4.isChecked = true
            binding.chipNewLan5.text -> binding.chipNewLan5.isChecked = true
            binding.chipNewLan6.text -> binding.chipNewLan6.isChecked = true
            binding.chipNewLan7.text -> binding.chipNewLan7.isChecked = true
            binding.chipNewLan8.text -> binding.chipNewLan8.isChecked = true
            binding.chipNewLan9.text -> binding.chipNewLan9.isChecked = true
        }

        binding.textLessonName.setText(lesson.name)
        binding.switchLessonSettingsReadOutBoth.isChecked = !lesson.settingReadOutBoth
        binding.switchLessonSettingsAskOnlyNewWords.isChecked = lesson.askOnlyNewWords


        binding.chipTypeLesson1.isChecked = lesson.typesOfLesson.contains(1)
        binding.chipTypeLesson2.isChecked = lesson.typesOfLesson.contains(2)
        binding.chipTypeLesson3.isChecked = lesson.typesOfLesson.contains(3)

        return binding.root
    }

    private fun saveLesson() {
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
                Log.i("EditLesson", "Keep name")
                binding.textLessonName.error = null
                binding.textLessonName.text.toString()
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


        val vocabularyGroupsIds: ArrayList<Int> = ArrayList()

        val index = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(requireContext()))

        if(arrayListGroup.isEmpty()){
            Toast.makeText(requireContext(), "Vokabelgruppe fehlt", Toast.LENGTH_SHORT).show()
            return
        }


        for(i in 0 until index.getJSONArray("index").length()) {
            if(arrayListGroup.contains(index.getJSONArray("index").getJSONObject(i).getString("name"))){
                vocabularyGroupsIds.add(index.getJSONArray("index").getJSONObject(i).getInt("id"))
            }
        }

        lesson.name = name
        lesson.languageKnow = langKnown
        lesson.languageNew = langNew
        lesson.vocabularyGroupIds = vocabularyGroupsIds.toTypedArray()
        lesson.settingReadOutBoth = settingReadOutBoth
        lesson.askOnlyNewWords = settingAskOnlyNewWords
        lesson.typesOfLesson = typesOfLesson

        lesson.saveInFile()
        findNavController().navigate(ManageLessonFragmentDirections.actionManageLessonFragmentToNavigationMain())
    }

    private fun addChipToGroup(groupName: String){
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
}