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
import java.io.File

class LessonBasicFragment: Fragment() {
    private var _binding: FragmentNewLessonBinding? = null
    private val binding get() = _binding!!

    private val args: LessonBasicFragmentArgs by navArgs()

    private val allVocabularyGroups = ArrayList<VocabularyGroup>()
    private val vocabularyGroupsSelected = ArrayList<VocabularyGroup>()
    private lateinit var lesson: Lesson


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewLessonBinding.inflate(layoutInflater, container, false)

        binding.buttonNext.apply {
            setOnClickListener { saveLesson() }
            text = if(args.mode == MODE_EDIT) getString(R.string.save) else getString(R.string.to_finish)
        }

        val index = JSONObject(FileUtil.loadFromFile(File(requireContext().filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)))

        allVocabularyGroups.clear()
        for(i in 0 until index.getJSONArray("index").length()) {
            VocabularyGroup.loadFromFileWithId(Id(index.getJSONArray("index").getJSONObject(i).getInt("id")), requireContext())?.let { allVocabularyGroups.add(it) }
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
            if(args.lesson != null){
                lesson = args.lesson!!
            }else{
                Log.e("LessonBasic", "Lesson is null while mode ${args.mode}, can't load lesson")
            }

            vocabularyGroupsSelected.clear()
            for(indexArray in 0 until index.getJSONArray("index").length()) {
                val groupIds = ArrayList<Int>()
                lesson.vocabularyGroups.forEach {
                    groupIds.add(it.id.number)
                }

                if (groupIds.contains(index.getJSONArray("index").getJSONObject(indexArray).getInt("id"))) {
                    VocabularyGroup.loadFromFileWithId(
                        Id(index.getJSONArray("index").getJSONObject(indexArray).getInt("id")), requireContext())?.let { vocabularyGroupsSelected.add(it) }
                }
            }

            vocabularyGroupsSelected.forEach{
                addChipToGroup(it)
            }

            binding.textLessonName.setText(lesson.name)
            binding.chipLessonSettingsChipReadFirstWords.isChecked = lesson.readOut.contains(Lesson.READ_OTHER_LANGUAGE to true)
            binding.chipLessonSettingsChipReadSecondWords.isChecked = lesson.readOut.contains(Lesson.READ_MAIN_LANGUAGE to true)
            binding.switchLessonSettingsAskOnlyNewWords.isChecked = lesson.isOnlyMainWordAskedAsAnswer


            binding.chipTypeLesson1.isChecked = lesson.typesOfExercises.contains(1)
            binding.chipTypeLesson2.isChecked = lesson.typesOfExercises.contains(2)
            binding.chipTypeLesson3.isChecked = lesson.typesOfExercises.contains(3)

            binding.chipLessonSettingsWordsToPracticeTranslation.isChecked = lesson.typesOfWordToPractice.contains(VocabularyWord.TYPE_TRANSLATION)
            binding.chipLessonSettingsWordsToPracticeSynonym.isChecked = lesson.typesOfWordToPractice.contains(VocabularyWord.TYPE_SYNONYM)
            binding.chipLessonSettingsWordsToPracticeAntonym.isChecked = lesson.typesOfWordToPractice.contains(VocabularyWord.TYPE_ANTONYM)
            binding.chipLessonSettingsWordsToPracticeWordFamily.isChecked = lesson.typesOfWordToPractice.contains(VocabularyWord.TYPE_WORD_FAMILY)

            binding.sliderCreateLessonNumberExercises.value = lesson.numberOfExercises.toFloat()

            binding.switchLessonSettingsRequestAllPossibleAnswer.isChecked = lesson.askForAllWords
        }else{
            binding.chipTypeLesson1.isChecked = true
            binding.chipTypeLesson2.isChecked = true
            binding.chipTypeLesson3.isChecked = true

            binding.chipLessonSettingsChipReadFirstWords.isChecked = true
            binding.chipLessonSettingsChipReadSecondWords.isChecked = true

            binding.chipLessonSettingsWordsToPracticeTranslation.isChecked = true
            binding.chipLessonSettingsWordsToPracticeSynonym.isChecked = true
            binding.chipLessonSettingsWordsToPracticeAntonym.isChecked = true
            binding.chipLessonSettingsWordsToPracticeWordFamily.isChecked = true
        }

        refreshList()
        return binding.root
    }

    private fun saveLesson() {
        val name: String = when(Lesson.isNameValid(requireContext(), binding.textLessonName.text.toString())){
            Lesson.VALID, Lesson.INVALID_NAME_ALREADY_USED -> {
                binding.textLessonNameLayout.error = null
                binding.textLessonName.text.toString()
            }
            Lesson.INVALID_TOO_MANY_CHARS -> {
                binding.textLessonNameLayout.error = getString(R.string.err_name_too_long_max, Lesson.MAX_CHARS)
                return
            }
            Lesson.INVALID_TOO_MANY_LINES -> {
                binding.textLessonNameLayout.error = getString(R.string.err_too_many_lines, Lesson.MAX_LINES)
                return
            }
            Lesson.INVALID_EMPTY -> {
                binding.textLessonNameLayout.error = getString(R.string.err_missing_name)
                return
            }
            else -> {
                Toast.makeText(requireContext(), getString(R.string.err), Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Settings

        // If selected is it false
        val settingReadOut = arrayListOf<Pair<Int, Boolean>>()

        settingReadOut.add(Lesson.READ_OTHER_LANGUAGE to binding.chipGroupLessonSettingsReadOutBoth.checkedChipIds.contains(binding.chipLessonSettingsChipReadFirstWords.id))
        settingReadOut.add(Lesson.READ_MAIN_LANGUAGE to binding.chipGroupLessonSettingsReadOutBoth.checkedChipIds.contains(binding.chipLessonSettingsChipReadSecondWords.id))

        val settingAskOnlyNewWords = binding.switchLessonSettingsAskOnlyNewWords.isChecked
        val numberOfExercises = binding.sliderCreateLessonNumberExercises.value.toInt()

        val typesOfLesson: ArrayList<Int> = arrayListOf()
        if(binding.chipTypeLesson1.isChecked)
            typesOfLesson.add(1)
        if(binding.chipTypeLesson2.isChecked)
            typesOfLesson.add(2)
        if(binding.chipTypeLesson3.isChecked)
            typesOfLesson.add(3)

        val typesOfWords = arrayListOf<Int>()
        if(binding.chipLessonSettingsWordsToPracticeTranslation.isChecked)
            typesOfWords.add(VocabularyWord.TYPE_TRANSLATION)
        if(binding.chipLessonSettingsWordsToPracticeSynonym.isChecked)
            typesOfWords.add(VocabularyWord.TYPE_SYNONYM)
        if(binding.chipLessonSettingsWordsToPracticeAntonym.isChecked)
            typesOfWords.add(VocabularyWord.TYPE_ANTONYM)
        if(binding.chipLessonSettingsWordsToPracticeWordFamily.isChecked)
            typesOfWords.add(VocabularyWord.TYPE_WORD_FAMILY)

        val askForAllWords = binding.switchLessonSettingsRequestAllPossibleAnswer.isChecked

        if(typesOfLesson.isEmpty()){
            Toast.makeText(requireContext(), R.string.err_need_one_type_of_lesson, Toast.LENGTH_LONG).show()
            return
        }

        if(vocabularyGroupsSelected.isEmpty()){
            Toast.makeText(requireContext(), R.string.err_missing_vocabulary_group, Toast.LENGTH_SHORT).show()
            return
        }

        val vocabularyGroups = ArrayList<VocabularyGroup>()
        vocabularyGroupsSelected.forEach {
            vocabularyGroups.add(it)
        }

        if(args.mode == MODE_CREATE){
            lesson = Lesson(name, Id.generate(requireContext()).apply { register(requireContext()) }, typesOfExercises = typesOfLesson, vocabularyGroups =  vocabularyGroups, readOut = settingReadOut, askForAllWords =  askForAllWords , isOnlyMainWordAskedAsAnswer = settingAskOnlyNewWords, numberOfExercises = numberOfExercises, typesOfWordToPractice = typesOfWords)
            lesson.saveInIndex(requireContext())
        }else{
            lesson.name = name
            lesson.vocabularyGroups = vocabularyGroups
            lesson.readOut = settingReadOut
            lesson.isOnlyMainWordAskedAsAnswer = settingAskOnlyNewWords
            lesson.typesOfExercises = typesOfLesson
            lesson.numberOfExercises = numberOfExercises
            lesson.askForAllWords = askForAllWords
            lesson.typesOfWordToPractice = typesOfWords
        }

        lesson.saveInFile(requireContext())
        findNavController().navigate(LessonBasicFragmentDirections.actionManageLessonFragmentToNavigationMain())
    }

    private fun addChipToGroup(vocabularyGroup: VocabularyGroup){
        val chip = Chip(requireContext()).apply {
            text = vocabularyGroup.name
            chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_book_open_page_variant_outline)
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
                findNavController().navigate(LessonBasicFragmentDirections.actionGlobalNewVocabularyGroupFragment(vocabularyGroup, keyMode = VocabularyGroupBasicFragment.MODE_EDIT))
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