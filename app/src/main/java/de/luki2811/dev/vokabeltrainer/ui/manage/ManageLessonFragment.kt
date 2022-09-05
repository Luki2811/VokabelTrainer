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
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentNewLessonBinding
import de.luki2811.dev.vokabeltrainer.ui.create.NewVocabularyGroupFragment
import org.json.JSONObject
import java.io.File

class ManageLessonFragment: Fragment() {
    private var _binding: FragmentNewLessonBinding? = null
    private val binding get() = _binding!!

    private val args: ManageLessonFragmentArgs by navArgs()

    private val allVocabularyGroups = ArrayList<VocabularyGroup>()
    private val vocabularyGroupsSelected = ArrayList<VocabularyGroup>()
    private lateinit var lesson: Lesson


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewLessonBinding.inflate(layoutInflater, container, false)

        binding.buttonNext.apply {
            setOnClickListener { saveLesson() }
            text = if(args.mode == MODE_EDIT) getString(R.string.save) else getString(R.string.to_finish)
        }

        val index = JSONObject(AppFile.loadFromFile(File(requireContext().filesDir, AppFile.NAME_FILE_INDEX_VOCABULARY_GROUPS)))

        allVocabularyGroups.clear()
        for(i in 0 until index.getJSONArray("index").length()) {
            VocabularyGroup.loadFromFileWithId(Id(requireContext(),index.getJSONArray("index").getJSONObject(i).getInt("id")), requireContext())?.let { allVocabularyGroups.add(it) }
        }

        binding.buttonAddVocabularyGroupToLesson.setOnClickListener {
            allVocabularyGroups.forEach {
                if(binding.autoCompleteTextVocabularyGroups.text.toString() == it.name && !vocabularyGroupsSelected.contains(it)){
                    addChipToGroup(it)
                    vocabularyGroupsSelected.add(it)
                    binding.autoCompleteTextVocabularyGroups.text = null
                }
            }
            refreshList()
        }

        if(args.mode == MODE_EDIT){
            lesson = Lesson(JSONObject(args.lessonJson), requireContext())

            vocabularyGroupsSelected.clear()
            for(indexArray in 0 until index.getJSONArray("index").length())
                if(lesson.vocabularyGroupIds.contains(index.getJSONArray("index").getJSONObject(indexArray).getInt("id"))) {
                    VocabularyGroup.loadFromFileWithId(Id(requireContext(), index.getJSONArray("index").getJSONObject(indexArray).getInt("id")),requireContext())?.let { vocabularyGroupsSelected.add(it) }
                }

            vocabularyGroupsSelected.forEach{
                addChipToGroup(it)
            }

            binding.textLessonName.setText(lesson.name)
            binding.switchLessonSettingsReadOutBoth.isChecked = !lesson.settingReadOutBoth
            binding.switchLessonSettingsAskOnlyNewWords.isChecked = lesson.askOnlyNewWords


            binding.chipTypeLesson1.isChecked = lesson.typesOfLesson.contains(1)
            binding.chipTypeLesson2.isChecked = lesson.typesOfLesson.contains(2)
            binding.chipTypeLesson3.isChecked = lesson.typesOfLesson.contains(3)

            binding.sliderCreateLessonNumberExercises.value = lesson.numberOfExercises.toFloat()
        }

        refreshList()
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
        val settingReadOutBoth = !binding.switchLessonSettingsReadOutBoth.isChecked
        val settingAskOnlyNewWords = binding.switchLessonSettingsAskOnlyNewWords.isChecked
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

        if(vocabularyGroupsSelected.isEmpty()){
            Toast.makeText(requireContext(), R.string.err_missing_vocabulary_group, Toast.LENGTH_SHORT).show()
            return
        }

        val vocabularyGroupsIds = ArrayList<Int>()
        vocabularyGroupsSelected.forEach {
            vocabularyGroupsIds.add(it.id.number)
        }

        if(args.mode == MODE_CREATE){
            lesson = Lesson(name, vocabularyGroupsIds.toTypedArray(), requireContext(), settingReadOutBoth, settingAskOnlyNewWords, typesOfLesson, numberOfExercises = numberOfExercises)
            lesson.saveInIndex()
        }else{
            lesson.name = name
            lesson.vocabularyGroupIds = vocabularyGroupsIds.toTypedArray()
            lesson.settingReadOutBoth = settingReadOutBoth
            lesson.askOnlyNewWords = settingAskOnlyNewWords
            lesson.typesOfLesson = typesOfLesson
            lesson.numberOfExercises = numberOfExercises
        }

        lesson.saveInFile()
        findNavController().navigate(ManageLessonFragmentDirections.actionManageLessonFragmentToNavigationMain())
    }

    private fun addChipToGroup(vocabularyGroup: VocabularyGroup){
        val chip = Chip(requireContext()).apply {
            text = vocabularyGroup.name
            chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.book_open_page_variant_outline)
            isCloseIconVisible = true
            isChipIconVisible = false
            setTextAppearanceResource(R.style.chipText)
            isClickable = true
            isCheckable = false

            setOnCloseIconClickListener {
                vocabularyGroupsSelected.remove(vocabularyGroup)
                binding.chipGroupSelectedVocabularyGroups.removeView(this as View)
                refreshList()
            }
            setOnClickListener {
                findNavController().navigate(ManageLessonFragmentDirections.actionGlobalNewVocabularyGroupFragment(vocabularyGroup.getAsJson().toString(), keyMode = NewVocabularyGroupFragment.MODE_EDIT))
            }
        }

        binding.chipGroupSelectedVocabularyGroups.addView(chip as View)
    }

    private fun refreshList(){
        val names = ArrayList<String>()
        allVocabularyGroups.filter { !vocabularyGroupsSelected.contains(it) }.forEach {
            names.add(it.name)
        }

        binding.autoCompleteTextVocabularyGroups.setAdapter(ArrayAdapter(requireContext(), R.layout.default_list_item, names.toTypedArray().sortedBy { it }))

    }

    companion object{
        const val MODE_CREATE = 0
        const val MODE_EDIT = 1
        // const val MODE_IMPORT = 2
    }
}