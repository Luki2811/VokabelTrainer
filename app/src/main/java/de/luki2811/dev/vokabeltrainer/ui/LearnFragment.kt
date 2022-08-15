package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.adapter.ListLessonsLearnAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentLearnBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ListLessonsLearnAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)

        binding.listOfLessonsCards.layoutManager = LinearLayoutManager(requireContext())

        val indexAsJson = JSONObject(AppFile.loadFromFile(File(requireContext().filesDir, AppFile.NAME_FILE_INDEX_LESSONS)))
        val arrayList = ArrayList<Lesson>()

        try {
            for(i in 0 until indexAsJson.getJSONArray("index").length()){
                var file = File(requireContext().filesDir, "lessons")
                file.mkdirs()
                file = File(file, indexAsJson.getJSONArray("index").getJSONObject(i).getInt("id").toString() + ".json" )
                val jsonOfVocGroup = JSONObject(AppFile.loadFromFile(file))
                arrayList.add(Lesson(jsonOfVocGroup, requireContext()))
            }
        }catch (e: JSONException){
            e.printStackTrace()
        }

        if(arrayList.isEmpty()){
            binding.searchView.visibility = View.GONE
            binding.listOfLessonsCards.visibility = View.GONE
            binding.textViewLearnFragmentInfo.visibility = View.VISIBLE
            binding.textViewLearnFragmentInfo.text = getString(R.string.create_lesson_or_vocabulary_group)

            return binding.root
        }

        adapter = ListLessonsLearnAdapter(arrayList, findNavController(), requireContext(), requireActivity())

        binding.listOfLessonsCards.adapter = adapter

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}