package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
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
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewLessonBinding
import org.json.JSONObject
import java.io.File

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

        val index = JSONObject(AppFile.loadFromFile(File(requireContext().filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)))

        arrayList = ArrayList()
        for(i in 0 until index.getJSONArray("index").length()) {
            arrayList.add(index.getJSONArray("index").getJSONObject(i).getString("name"))
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.default_list_item, arrayList.toTypedArray().sortedArray())
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

        binding.textLessonName.setText(lesson.name)
        binding.switchLessonSettingsReadOutBoth.isChecked = !lesson.settingReadOutBoth
        binding.switchLessonSettingsAskOnlyNewWords.isChecked = lesson.askOnlyNewWords


        binding.chipTypeLesson1.isChecked = lesson.typesOfLesson.contains(1)
        binding.chipTypeLesson2.isChecked = lesson.typesOfLesson.contains(2)
        binding.chipTypeLesson3.isChecked = lesson.typesOfLesson.contains(3)

        binding.sliderCreateLessonNumberExercises.value = lesson.numberOfExercises.toFloat()

        return binding.root
    }

    private fun saveLesson() {
        val name: String = when(Lesson.isNameValid(requireContext(), binding.textLessonName.text.toString())){
            0 -> {
                binding.textLessonName.error = null
                binding.textLessonName.text.toString()
            }
            1 -> {
                binding.textLessonName.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
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

        // Settings

        // If selected is it false
        val settingReadOutBoth: Boolean = !binding.switchLessonSettingsReadOutBoth.isChecked

        val settingAskOnlyNewWords: Boolean = binding.switchLessonSettingsAskOnlyNewWords.isChecked

        val numberOfExercises = binding.sliderCreateLessonNumberExercises.value.toInt()

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

        val index = JSONObject(AppFile.loadFromFile(File(requireContext().filesDir,AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)))

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
        lesson.vocabularyGroupIds = vocabularyGroupsIds.toTypedArray()
        lesson.settingReadOutBoth = settingReadOutBoth
        lesson.askOnlyNewWords = settingAskOnlyNewWords
        lesson.typesOfLesson = typesOfLesson
        lesson.numberOfExercises = numberOfExercises

        lesson.saveInFile()
        findNavController().navigate(ManageLessonFragmentDirections.actionManageLessonFragmentToNavigationMain())
    }

    private fun addChipToGroup(groupName: String){
        val chip = Chip(requireContext())
        chip.text = groupName
        chip.chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.book_open_page_variant_outline)
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