package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithURLBinding
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ImportWithURLFragment : Fragment() {

    private var _binding: FragmentImportWithURLBinding? = null
    private val binding get() = _binding!!
    private var stringDownloaded = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportWithURLBinding.inflate(layoutInflater, container, false)

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
            stringDownloaded = download(urlToDownload)
            requireActivity().runOnUiThread { if(stringDownloaded.isNotEmpty()) {
                binding.textViewImportUrlTemp.text = stringDownloaded
            } else{
                binding.textViewImportUrlTemp.setTextColor(requireActivity().getColor(R.color.Red))
                binding.textViewImportUrlTemp.text = getString(R.string.err_URL_has_no_content)
            }
            }
        }.start()
        binding.textViewImportUrlTemp.setText(R.string.download_started)
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