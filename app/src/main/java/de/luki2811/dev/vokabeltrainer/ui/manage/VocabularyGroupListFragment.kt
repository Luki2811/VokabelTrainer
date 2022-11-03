package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import de.luki2811.dev.vokabeltrainer.FileUtil
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.adapter.ListVocabularyGroupsAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageVocabularyGroupsBinding
import org.json.JSONObject
import java.io.File

class VocabularyGroupListFragment : Fragment() {

    private var _binding: FragmentManageVocabularyGroupsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageVocabularyGroupsBinding.inflate(inflater, container, false)

        binding.buttonManageCreateNewVocabularyGroup.setOnClickListener {
            findNavController().navigate(VocabularyGroupListFragmentDirections.actionManageVocabularyGroupsFragmentToNavigationCreate())
        }

        val arrayList = ArrayList<VocabularyGroup>()
        val indexVocabularyGroupFile = File(requireContext().filesDir, FileUtil.NAME_FILE_INDEX_VOCABULARY_GROUPS)
        val indexAsJson = JSONObject(FileUtil.loadFromFile(indexVocabularyGroupFile))

        binding.listOfVocabularyGroups.layoutManager = LinearLayoutManager(requireContext())

        for(i in 0 until indexAsJson.getJSONArray("index").length()){
            var file = File(requireContext().filesDir, "vocabularyGroups")
            file.mkdirs()
            file = File(file, indexAsJson.getJSONArray("index").getJSONObject(i).getInt("id").toString() + ".json" )
            val jsonOfVocGroup = JSONObject(FileUtil.loadFromFile(file))
            arrayList.add(VocabularyGroup(jsonOfVocGroup, context =  requireContext()))
        }

        val adapter = ListVocabularyGroupsAdapter(arrayList, requireContext(),findNavController(), requireActivity().supportFragmentManager)
        binding.listOfVocabularyGroups.adapter = adapter

        binding.searchViewManageVocabularyGroupsLayout.apply {
            endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            isErrorEnabled = false
            isStartIconVisible = true
            startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_search_24)
        }

        binding.searchViewManageVocabularyGroups.addTextChangedListener {
            if(it.isNullOrEmpty()){
                adapter.filter.filter("")
            }else{
                adapter.filter.filter(it.toString())
            }
        }

        return binding.root

    }
}

