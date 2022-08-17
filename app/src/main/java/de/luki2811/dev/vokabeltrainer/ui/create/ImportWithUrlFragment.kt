package de.luki2811.dev.vokabeltrainer.ui.create

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithURLBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.*
import javax.net.ssl.HttpsURLConnection

class ImportWithUrlFragment : Fragment() {

    private var _binding: FragmentImportWithURLBinding? = null
    private val binding get() = _binding!!
    private var stringDownloaded = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportWithURLBinding.inflate(layoutInflater, container, false)

        binding.progressBarImportDownload.visibility = View.GONE

        binding.buttonImportFromURL.setOnClickListener { startImport() }
        binding.textEditUrlToImportLayout.helperText = "https://www.example.com/vocabulary/xxxxxx.json"
        binding.textEditUrlToImport.addTextChangedListener {
            if(it.toString().startsWith("https://")) {
                binding.textEditUrlToImportLayout.prefixText = null
            }else {
                binding.textEditUrlToImportLayout.prefixText = "https://"
            }
        }

        return binding.root
    }

    private fun startImport(){
        val urlToDownload: URL

        binding.progressBarImportDownload.visibility = View.VISIBLE

        binding.textViewImportUrlTemp.setTextColor(requireActivity().getColor(R.color.White))

        try {
            urlToDownload = if(binding.textEditUrlToImportLayout.prefixText != null)
                URL("https://"+binding.textEditUrlToImport.text.toString())
            else
                URL(binding.textEditUrlToImport.text.toString())

        }catch (e: MalformedURLException){
            binding.textEditUrlToImportLayout.error = getString(R.string.err_cant_parse_to_URL)
            return
        }
        binding.textEditUrlToImportLayout.error = null

        Thread{
            if(isOnline())
                stringDownloaded = download(urlToDownload)
            else {
                binding.textViewImportUrlTemp.setTextColor(requireActivity().getColor(R.color.Red))
                requireActivity().runOnUiThread {
                    binding.textViewImportUrlTemp.text = getString(R.string.err_no_internet_connection)
                    binding.progressBarImportDownload.visibility = View.GONE
                }
                return@Thread
            }
            requireActivity().runOnUiThread {
                if(stringDownloaded.isNotEmpty()) {
                    val vocabularyGroup: VocabularyGroup
                    // Create new Lesson
                    try {
                        val dataAsJson = JSONObject(stringDownloaded)
                        when(dataAsJson.getInt("type")){
                            AppFile.TYPE_FILE_UNKNOWN -> {
                                Toast.makeText(requireContext(), getText(R.string.err_unknown_type), Toast.LENGTH_LONG).show()
                                cancelImportLesson()
                            }

                            // Import vocabulary group

                            AppFile.TYPE_FILE_VOCABULARY_GROUP -> {
                                try {
                                    if(dataAsJson.getJSONArray("vocabulary").length() >= 2){
                                        vocabularyGroup = VocabularyGroup(dataAsJson, context = requireContext())
                                        Toast.makeText(requireContext(), getString(R.string.download_successfully), Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(ImportWithUrlFragmentDirections.actionImportWithUrlFragmentToNewVocabularyGroupFragment(vocabularyGroup.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT))
                                    }
                                } catch (e: JSONException){
                                    Toast.makeText(requireContext(), getText(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
                                    cancelImportLesson()
                                }
                            }

                            // Import lesson

                            AppFile.TYPE_FILE_LESSON -> {
                                try {
                                    val newIdsVocabularyGroups = arrayListOf<Int>()
                                    val vocabularyGroups = dataAsJson.getJSONArray("vocabularyGroups")
                                    for (i in 0 until vocabularyGroups.length()){
                                        val vocGroupFromLesson = VocabularyGroup(vocabularyGroups.getJSONObject(i), context = requireContext(), generateNewId = true)

                                        // Log.e("Import TEMP", "VocGroup ID: ${vocGroupFromLesson.id.number} (${vocGroupFromLesson.name}) \ni/length ${i+1}/${vocabularyGroups.length()}")

                                        var tempInt = 0
                                        while(VocabularyGroup.isNameValid(requireContext(), vocGroupFromLesson.name) != 0){
                                            tempInt += 1
                                            var nameOfVocGroup = vocGroupFromLesson.name
                                            nameOfVocGroup = if(tempInt > 1)
                                                nameOfVocGroup.replace("(${tempInt - 1})","(${tempInt})")
                                            else
                                                "${vocGroupFromLesson.name} (1)"

                                            vocGroupFromLesson.name = nameOfVocGroup
                                        }
                                        Log.i("Import ID", vocGroupFromLesson.id.number.toString())
                                        newIdsVocabularyGroups.add(vocGroupFromLesson.id.number)
                                        vocGroupFromLesson.saveInFile()
                                        vocGroupFromLesson.saveInIndex()
                                    }

                                    var nameOfLesson = dataAsJson.getString("name")
                                    var tempInt = 0
                                    while(Lesson.isNameValid(requireContext(), nameOfLesson) != 0){
                                        tempInt += 1

                                        nameOfLesson = if(tempInt > 1)
                                            nameOfLesson.replace("(${tempInt - 1})","(${tempInt})")
                                        else
                                            "${dataAsJson.getString("name")} (1)"

                                    }
                                    val askOnlyNewWords = dataAsJson.getJSONObject("settings").getBoolean("askOnlyNewWords")
                                    val readOutBoth = dataAsJson.getJSONObject("settings").getBoolean("readOutBoth")
                                    val numberOfExercises = try {
                                        dataAsJson.getJSONObject("settings").getInt("numberOfExercises")
                                    } catch (e: JSONException){
                                        10
                                    }

                                    val useTypes = arrayListOf<Int>()
                                    if(dataAsJson.getJSONObject("settings").getBoolean("useType1")) useTypes.add(
                                        Exercise.TYPE_TRANSLATE_TEXT)
                                    if(dataAsJson.getJSONObject("settings").getBoolean("useType2")) useTypes.add(
                                        Exercise.TYPE_CHOOSE_OF_THREE_WORDS)
                                    if(dataAsJson.getJSONObject("settings").getBoolean("useType3")) useTypes.add(
                                        Exercise.TYPE_MATCH_FIVE_WORDS)

                                    val lesson = Lesson(nameOfLesson, newIdsVocabularyGroups.toTypedArray() , requireContext(), readOutBoth, askOnlyNewWords, useTypes, numberOfExercises = numberOfExercises)
                                    lesson.saveInFile()
                                    lesson.saveInIndex()

                                    Toast.makeText(requireContext(), R.string.import_lesson_successful, Toast.LENGTH_LONG).show()

                                    findNavController().navigate(R.id.action_importWithURLFragment_pop)

                                } catch (e: JSONException){
                                    Toast.makeText(requireContext(), getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
                                    e.printStackTrace()
                                    cancelImportLesson()
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        binding.textViewImportUrlTemp.setTextColor(requireActivity().getColor(R.color.Red))
                        binding.textViewImportUrlTemp.text = getString(R.string.err_could_not_import_vocabulary_group)
                        binding.progressBarImportDownload.visibility = View.GONE

                    }

                } else{
                    binding.textViewImportUrlTemp.setTextColor(requireActivity().getColor(R.color.Red))
                    binding.textViewImportUrlTemp.text = getString(R.string.err_URL_has_no_content)
                    binding.progressBarImportDownload.visibility = View.GONE
                }
            }
        }.start()
        binding.textViewImportUrlTemp.setText(R.string.download_started)
    }

    private fun cancelImportLesson(){
        findNavController().navigate(R.id.action_importWithURLFragment_pop)
    }

    private fun isOnline(): Boolean{
        return try {
            val timeoutMs = 1500
            val sock = Socket()
            val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
            sock.connect(sockaddr, timeoutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }

    }

    private fun download(url: URL): String{
        return try {
            val connection = url.openConnection() as HttpsURLConnection
            connection.inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        }catch (e: IOException){
            e.printStackTrace()

            requireActivity().runOnUiThread {
                binding.textEditUrlToImport.error = getString(R.string.err_cant_find_URL)
            }
            ""
        }
    }


}