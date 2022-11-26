package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import de.luki2811.dev.vokabeltrainer.FileUtil
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.adapter.ListLessonsLearnAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentLearnBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ListLessonsLearnAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)


        var allLessons = Lesson.loadAllLessons(requireContext())

        setFragmentResultListener("finishImportOfLesson"){ _, _ ->
            clearFragmentResult("finishImportOfLesson")
            allLessons = Lesson.loadAllLessons(requireContext())
            binding.listOfLessonsCards.adapter?.notifyItemInserted(allLessons.size-1)
        }

        binding.listOfLessonsCards.layoutManager = LinearLayoutManager(requireContext())


        binding.buttonPracticeMistakes.setOnClickListener {
            findNavController().navigate(R.id.action_global_createPracticeFragment)
        }

        if(allLessons.isEmpty()){
            binding.searchViewLearnLayout.visibility = View.GONE
            binding.searchViewLearn.visibility = View.GONE
            binding.listOfLessonsCards.visibility = View.GONE
            binding.buttonPracticeMistakes.visibility = View.GONE
            binding.textViewLearnFragmentInfo.visibility = View.VISIBLE
            binding.textViewLearnFragmentInfo.text = getString(R.string.create_lesson_or_vocabulary_group)

            return binding.root
        }

        adapter = ListLessonsLearnAdapter(allLessons, findNavController(), requireContext(), requireActivity())

        binding.listOfLessonsCards.adapter = adapter

        binding.searchViewLearnLayout.apply {
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            isErrorEnabled = false
            isStartIconVisible = true
            startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_search_24)
        }

        binding.searchViewLearn.addTextChangedListener {
            if(it.isNullOrEmpty()){
                adapter.filter.filter("")
            }else{
                adapter.filter.filter(it.toString())
            }
        }

        /** binding.searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        }) **/
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}