package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.loadFromFile
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.URI

class ImportFragment : Fragment() {

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!

    private lateinit var vocabularyGroup: VocabularyGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportBinding.inflate(inflater, container, false)

        val context = requireContext()

        binding.buttonImportVocabularyGroupCancel.setOnClickListener { cancelImportLesson() }
        binding.buttonImportVocabularyGroupFinish.setOnClickListener { finishImport() }


        val uri: Uri = Uri.parse(arguments?.getString("KEY_DATA"))
        println(uri.toString())
        val file = File(RealPathUtil.getRealPath(context, uri)!!)

        // Create new Lesson
        try {
            val groupAsJSON = JSONObject(loadFromFile(file))
            if(groupAsJSON.getJSONArray("vocabulary").length() >= 2){
                vocabularyGroup = VocabularyGroup(groupAsJSON, requireContext())
                binding.editVocabularyGroupNameImport.setText(vocabularyGroup.name)
                binding.editVocabularyGroupNameImport.hint = vocabularyGroup.name
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(context, getText(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_importFragment_to_createNewMainFragment)
        }
        return binding.root
    }

    private fun finishImport() {
        val context = requireContext()

        // Create and register ID
        vocabularyGroup.id = Id(context)

        // set Name
        when (VocabularyGroup.isNameValid(requireContext(), binding.editVocabularyGroupNameImport)) {
            0 -> {
                binding.editVocabularyGroupNameImport.error = null
                vocabularyGroup.name = binding.editVocabularyGroupNameImport.text.toString()
            }
            1 -> {
                binding.editVocabularyGroupNameImport.error = getString(R.string.err_name_contains_wrong_letter)
                return
            }
            2 -> {
                binding.editVocabularyGroupNameImport.error = getString(R.string.err_name_already_taken)
                return
            }
            3 -> {
                binding.editVocabularyGroupNameImport.error = getString(R.string.err_name_too_long_max, 50)
                return
            }
            4 -> {
                binding.editVocabularyGroupNameImport.error = getString(R.string.err_missing_name)
                return
            }
        }

        // Save in Index
        vocabularyGroup.saveInIndex(context)

        // Save lesson as .json
        var file = File(requireContext().filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, vocabularyGroup.id.number.toString() + ".json" )
        AppFile.writeInFile(vocabularyGroup.getAsJson().toString(), file)

        startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    private fun cancelImportLesson(){
        findNavController().navigate(R.id.action_importFragment_to_createNewMainFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}