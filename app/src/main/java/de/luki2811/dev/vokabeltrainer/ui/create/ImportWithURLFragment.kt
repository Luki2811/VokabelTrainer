package de.luki2811.dev.vokabeltrainer.ui.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithURLBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.*
import javax.net.ssl.HttpsURLConnection

class ImportWithURLFragment : Fragment() {

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
                    // Create new Lesson
                    try {
                        val groupAsJSON = JSONObject(stringDownloaded)
                        if(groupAsJSON.getJSONArray("vocabulary").length() >= 2){
                            val vocabularyGroup = VocabularyGroup(groupAsJSON, context = requireContext())
                            Toast.makeText(requireContext(), getString(R.string.download_successfully), Toast.LENGTH_SHORT).show()
                            findNavController().navigate(ImportWithURLFragmentDirections.actionImportWithURLFragmentToNewVocabularyGroupFragment2(vocabularyGroup.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT))
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