package de.luki2811.dev.vokabeltrainer

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.databinding.FragmentEditVocabularyGroupBinding
import org.json.JSONObject
import java.io.File

class EditVocabularyGroupFragment : Fragment() {

    private var _binding: FragmentEditVocabularyGroupBinding? = null
    private val binding get() = _binding!!
    lateinit var vocabularyGroup: VocabularyGroup
    lateinit var vocabulary: ArrayList<VocabularyWord>
    var pos: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditVocabularyGroupBinding.inflate(inflater, container, false)
        vocabularyGroup =
            VocabularyGroup(JSONObject(arguments?.getString("key_voc_group")!!), requireContext())

        vocabulary = ArrayList(vocabularyGroup.vocabulary.asList())

        binding.buttonBackVocabularyWord.setOnClickListener {
            if(refreshVocabularyWord()){
                pos -= 1
                refresh()
            }

        }

        binding.buttonNextVocabularyWord.setOnClickListener {
            if(refreshVocabularyWord()){
                pos += 1
                refresh()
            }
        }

        binding.buttonAddNewVocabularyWordManage.setOnClickListener { addVocabularyWord() }
        binding.buttonSaveAndGoBackManage.setOnClickListener { finishEdit() }
        binding.buttonDeleteVocabularyWord.setOnClickListener { removeVocabularyWord() }
        binding.buttonDeleteVocabularyGroup.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_vocabulary_group))
                .setMessage(R.string.do_you_really_want_to_delete_vocabulary_group)
                .setPositiveButton(getString(R.string.delete)){ _: DialogInterface, _: Int ->
                    deleteVocabularyGroup()
                }
                .setNegativeButton(getString(R.string.cancel)){ _: DialogInterface, _: Int ->
                    Toast.makeText(requireContext(), getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        refresh()

        return binding.root
    }

    private fun refreshVocabularyWord(): Boolean {
        vocabulary[pos].newWord =
            binding.textEditVocabularyWordNewManage.text.toString().trim().ifBlank {
                binding.textEditVocabularyWordNewManage.error = getString(R.string.err_missing_name)
                return false
            }
        vocabulary[pos].knownWord =
            binding.editTextVocabularyWordKnownManage.text.toString().trim().ifBlank {
                binding.editTextVocabularyWordKnownManage.error = getString(R.string.err_missing_name)
                return false
            }
        vocabulary[pos].isIgnoreCase = binding.switchVocabularyWordIgnoreCaseManage.isChecked
        return true
    }

    private fun addVocabularyWord() {
        if(refreshVocabularyWord()){
            pos = vocabulary.size
            binding.textEditVocabularyWordNewManage.text = null
            binding.editTextVocabularyWordKnownManage.text = null
            vocabulary.add(VocabularyWord("", "", binding.switchVocabularyWordIgnoreCaseManage.isChecked))
            refresh()
        }

    }

    private fun deleteVocabularyGroup() {

        var file = File(requireContext().filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, vocabularyGroup.id.number.toString() + ".json" )
        if(!file.delete()){
            Toast.makeText(requireContext(), getString(R.string.err_could_not_delete_vocabulary_group), Toast.LENGTH_LONG).show()
            Log.w("Error", "Failed to delete vocabularyGroup with id ${vocabularyGroup.id}")
            return
        }
        vocabularyGroup.deleteFromIndex(requireContext())
        vocabularyGroup.id.deleteId()
        Log.i("Info", "Successfully deleted vocabularyGroup with id ${vocabularyGroup.id}")
        findNavController().navigate(R.id.action_editVocabularyGroupFragment_to_manageVocabularyGroupsFragment)
    }

    private fun removeVocabularyWord() {
        vocabulary.removeAt(pos)
        if(pos != 0)
            pos -= 1
        refresh()
    }

    private fun finishEdit(){
        refreshVocabularyWord()
        vocabularyGroup.vocabulary = vocabulary.toTypedArray()
        // Speichern in Datei
        // Name der Vokabelgruppe gleich der ID(.json)
        var file = File(requireContext().filesDir, "vocabularyGroups")
        file.mkdirs()
        file = File(file, vocabularyGroup.id.number.toString() + ".json" )
        AppFile.writeInFile(vocabularyGroup.getAsJson().toString(), file)

        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    private fun refresh() {
        binding.textEditVocabularyWordNewManage.error = null
        binding.editTextVocabularyWordKnownManage.error = null
        binding.editTextVocabularyWordKnownManage.setText(vocabulary[pos].knownWord)
        binding.textEditVocabularyWordNewManage.setText(vocabulary[pos].newWord)
        binding.switchVocabularyWordIgnoreCaseManage.isChecked = vocabulary[pos].isIgnoreCase
        binding.textViewNumberOfVocManage.text =
            getString(R.string.number_voc_of_rest, (pos + 1), vocabulary.size)

        when (pos) {
            vocabulary.size - 1 -> {
                binding.buttonNextVocabularyWord.isEnabled = false
                binding.buttonBackVocabularyWord.isEnabled = true
            }
            0 -> {
                binding.buttonBackVocabularyWord.isEnabled = false
                binding.buttonNextVocabularyWord.isEnabled = true
            }
            else -> {
                binding.buttonBackVocabularyWord.isEnabled = true
                binding.buttonNextVocabularyWord.isEnabled = true

            }
        }

        binding.buttonDeleteVocabularyWord.isEnabled = vocabulary.size > 2
        if(vocabulary.size <= 2){
            binding.buttonDeleteVocabularyWord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.Gray), android.graphics.PorterDuff.Mode.SRC_IN)
        }else{
            binding.buttonDeleteVocabularyWord.setColorFilter(ContextCompat.getColor(requireContext(), R.color.White), android.graphics.PorterDuff.Mode.SRC_IN)
        }

    }
}