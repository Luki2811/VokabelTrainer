package de.luki2811.dev.vokabeltrainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.adapter.ListVocabularyGroupsAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageVocabularyGroupsBinding
import org.json.JSONObject
import java.io.File

class ManageVocabularyGroupsFragment : Fragment() {

    private var _binding: FragmentManageVocabularyGroupsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageVocabularyGroupsBinding.inflate(inflater, container, false)
        val arrayList = ArrayList<VocabularyGroup>()

        val indexAsJson = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_VOCABULARYGROUPS).loadFromFile(requireContext()))

        binding.listOfVocabularyGroups.layoutManager = LinearLayoutManager(requireContext())
        println(indexAsJson.toString())

        for(i in 0 until indexAsJson.getJSONArray("index").length()){
            var file = File(requireContext().filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, indexAsJson.getJSONArray("index").getJSONObject(i).getInt("id").toString() + ".json" )
            val jsonOfVocGroup = JSONObject(AppFile.loadFromFile(file))
            println(jsonOfVocGroup.toString())
            arrayList.add(VocabularyGroup(jsonOfVocGroup, requireContext()))
        }

        val adapter = ListVocabularyGroupsAdapter(arrayList.toTypedArray(), findNavController(), requireContext())
        binding.listOfVocabularyGroups.adapter = adapter

        return binding.root

    }
}