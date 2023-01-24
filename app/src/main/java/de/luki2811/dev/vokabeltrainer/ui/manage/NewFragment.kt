package de.luki2811.dev.vokabeltrainer.ui.manage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import de.luki2811.dev.vokabeltrainer.FileUtil
import de.luki2811.dev.vokabeltrainer.Importer
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreateNewMainBinding
import java.io.IOException
import java.net.*
import javax.net.ssl.HttpsURLConnection

class NewFragment : Fragment() {

    private var _binding: FragmentCreateNewMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataToImport: String

    private val args: NewFragmentArgs by navArgs()

    private val chooser: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json"))
    }

    // QR-Code
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            val image: InputImage
            try {
                image = InputImage.fromFilePath(requireContext(), uri)
                val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
                val scanner = BarcodeScanning.getClient(options)
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if(barcodes.isNullOrEmpty()){
                            Toast.makeText(requireContext(), getString(R.string.err_no_barcode_found), Toast.LENGTH_LONG).show()
                        }else{
                            barcodes.forEach {
                                if(it.rawValue.isNullOrEmpty()) {
                                    Toast.makeText(requireContext(), R.string.err_cant_load_qr_code, Toast.LENGTH_LONG).show()
                                }else{
                                    setDataAndSetupViews(it.rawValue!!)
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(it.localizedMessage)
                            .setIcon(R.drawable.ic_outline_error_24)
                            .setTitle(R.string.err)
                            .setPositiveButton(R.string.ok){ _, _ -> }
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else { Log.d("PhotoPicker", "No media selected") }
    }

    private val resultLauncherFilePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val intent = result.data
            setDataAndSetupViews(FileUtil.loadFromFile(intent?.data!!, requireActivity().contentResolver))
        } else {
            Log.d("FilePicker", "No file selected or file not found")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNewMainBinding.inflate(inflater, container, false)

        // Handle files
        if(args.dataToImport != null){
            startFinalImport(FileUtil.loadFromFile(args.dataToImport!!, requireActivity().contentResolver))
        }

        setupViews()

        return binding.root
    }

    private fun setDataAndSetupViews(data: String){
        dataToImport = data

        binding.buttonGeneralContinue.isEnabled = true
        Toast.makeText(requireContext(), getString(R.string.click_to_continue), Toast.LENGTH_LONG).show()
    }

    private fun setupViews() {
        // General
        binding.buttonGeneralContinue.apply {
            setOnClickListener {
                startFinalImport(dataToImport)
            }
            isEnabled = false
        }

        // Create

        /** binding.buttonCreateVocabularyGroupFromPicture.setOnClickListener {
            findNavController().navigate(NewFragmentDirections.actionCreateNewMainFragmentToCreateVocabularyGroupWithImageInfoFragment())
        } **/

        binding.buttonCreateVocabularyGroupFromPicture.isEnabled = false

        binding.buttonCreateLesson.setOnClickListener {
            findNavController().navigate(NewFragmentDirections.actionCreateNewMainFragmentToManageLessonFragment(mode = LessonBasicFragment.MODE_CREATE, lesson = null))
        }
        binding.buttonCreateVocabularyGroup.setOnClickListener {
            findNavController().navigate(NewFragmentDirections.actionCreateNewMainFragmentToNewVocabularyGroupFragment(null, VocabularyGroupBasicFragment.MODE_CREATE))
        }

        // File

        binding.buttonImportFileStart.apply {
            setOnClickListener { resultLauncherFilePicker.launch(chooser) }
        }

        // QR-Code

        binding.buttonImportQrCodeLoadImage.apply {
            isEnabled = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()
        }

        binding.buttonToggleGroupImportQrCode.apply {
            isSelectionRequired = true
            check(binding.buttonImportQrCodeScan.id)
        }

        binding.buttonImportQrCodeStart.apply {
            setOnClickListener {
                when(binding.buttonToggleGroupImportQrCode.checkedButtonId){
                    binding.buttonImportQrCodeLoadImage.id -> {
                        val mimeType = "image/*"
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
                    }
                    binding.buttonImportQrCodeScan.id -> {
                        val options = GmsBarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                        val scanner = GmsBarcodeScanning.getClient(requireContext(), options)
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                val result = barcode.rawValue
                                if(result.isNullOrEmpty())
                                    Toast.makeText(requireContext(), R.string.err_cant_load_qr_code, Toast.LENGTH_LONG).show()
                                else {
                                    setDataAndSetupViews(result)
                                }
                            }
                            .addOnCanceledListener { Log.i("GmsBarcodeScanning","Canceled") }
                            .addOnFailureListener { e ->
                                MaterialAlertDialogBuilder(requireContext())
                                    .setIcon(R.drawable.ic_outline_error_24)
                                    .setTitle(R.string.err)
                                    .setMessage(e.localizedMessage)
                                    .setPositiveButton(R.string.ok) { _, _ -> }
                                    .show()
                                e.printStackTrace()
                            }
                    }
                }
            }
        }

        // URL

        binding.buttonImportUrlStart.apply {
            setOnClickListener {
                val urlToDownload = try {
                    if(binding.textEditLayoutImportUrl.prefixText != null)
                        URL("https://"+binding.textEditImportUrl.text.toString())
                    else
                        URL(binding.textEditImportUrl.text.toString())

                }catch (e: MalformedURLException){
                    binding.textEditLayoutImportUrl.error = getString(R.string.err_cant_parse_to_URL)
                    return@setOnClickListener
                }
                binding.textEditLayoutImportUrl.error = null
                binding.progressBarImportUrl.visibility = View.VISIBLE
                binding.buttonImportUrlStart.visibility = View.GONE
                Thread{
                    importFromUrl(urlToDownload)
                }.start()
            }
        }

        binding.textEditLayoutImportUrl.apply {
            helperText = "https://www.example.com/vocabulary/xxxxxx.json"
        }

        binding.textEditImportUrl.addTextChangedListener {
            if(it.toString().startsWith("https://")) {
                binding.textEditLayoutImportUrl.prefixText = null
            }else {
                binding.textEditLayoutImportUrl.prefixText = "https://"
            }
        }
    }

    /**
     * DO NOT RUN ON UI THREAD
     */
    private fun importFromUrl(url: URL){
        val data: String = if(isOnline()){
            try {
                val connection = url.openConnection() as HttpsURLConnection
                connection.inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }catch (e: UnknownHostException){
                requireActivity().runOnUiThread {
                    cancelDownload()
                    binding.textEditLayoutImportUrl.error = getString(R.string.err_cant_find_URL)
                }
                return
            }catch (e: IOException){
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    cancelDownload()
                    binding.textEditLayoutImportUrl.error = getString(R.string.err)
                }
                return
            }
        }else{
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), getString(R.string.err_device_offline), Toast.LENGTH_LONG).show()
            }
            return
        }

        if(data.isEmpty()){
            requireActivity().runOnUiThread {
                binding.textEditLayoutImportUrl.error = getString(R.string.err_URL_has_no_content)
                cancelDownload()
            }
            return
        }

        requireActivity().runOnUiThread {
            binding.progressBarImportUrl.visibility = View.INVISIBLE
            binding.buttonImportUrlStart.visibility = View.VISIBLE
            setDataAndSetupViews(data)
        }
    }

    private fun cancelDownload(){
        binding.progressBarImportUrl.visibility = View.INVISIBLE
        binding.buttonImportUrlStart.visibility = View.VISIBLE
    }

    private fun isOnline(): Boolean{
        return try {
            val timeoutMs = 1500
            val sock = Socket()
            val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
            sock.connect(socketAddress, timeoutMs)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun startFinalImport(data: String){
        val importer = Importer(data, requireContext())
        when(importer.tryAll()){
            Importer.IMPORT_SUCCESSFULLY_VOCABULARY_GROUP -> {
                findNavController().navigate(NewFragmentDirections.actionCreateNewMainFragmentToNewVocabularyGroupFragment(importer.vocabularyGroup!!, VocabularyGroupBasicFragment.MODE_IMPORT))
            }
            Importer.IMPORT_SUCCESSFULLY_LESSON -> {
                Toast.makeText(context, R.string.import_lesson_successful, Toast.LENGTH_LONG).show()
                setFragmentResult("finishImportOfLesson", bundleOf())
                findNavController().popBackStack()
            }
            Importer.IMPORT_SUCCESSFULLY_SHORT_FORM -> {
                Toast.makeText(context, R.string.import_short_form_successful, Toast.LENGTH_LONG).show()
                findNavController().navigate(NewFragmentDirections.actionGlobalShortFormsManageFragment())
            }

            Importer.IMPORT_WRONG_OR_NONE_TYPE, Importer.IMPORT_NO_JSON -> {
                Toast.makeText(requireContext(), getString(R.string.err_import_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}