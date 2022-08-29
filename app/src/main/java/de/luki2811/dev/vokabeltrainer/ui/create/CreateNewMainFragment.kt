package de.luki2811.dev.vokabeltrainer.ui.create

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Importer
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreateNewMainBinding
import java.io.IOException
import java.net.*
import javax.net.ssl.HttpsURLConnection

class CreateNewMainFragment : Fragment() {

    private var _binding: FragmentCreateNewMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var dataToImport: String

    private val args: CreateNewMainFragmentArgs by navArgs()

    private val chooser: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/json"
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
                            Toast.makeText(requireContext(), "No barcodes found", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(requireContext(), "Failure", Toast.LENGTH_LONG).show()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else { Log.d("PhotoPicker", "No media selected") }
    }

    private val resultLauncherFilePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val intent = result.data
            setDataAndSetupViews(AppFile.loadFromFile(intent?.data!!, requireActivity().contentResolver))
        } else {
            Toast.makeText(requireContext(), getString(R.string.err_file_not_found), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNewMainBinding.inflate(inflater, container, false)

        // Handle files
        if(args.dataToImport != null){
            startFinalImport(AppFile.loadFromFile(args.dataToImport!!, requireActivity().contentResolver))
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

        binding.buttonCreateVocabularyGroupFromPicture.setOnClickListener {
            findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToCreateVocabularyGroupWithImageInfoFragment())
        }

        binding.buttonCreateLesson.setOnClickListener {
            findNavController().navigate(R.id.action_createNewMainFragment_to_newLessonFragment)
        }
        binding.buttonCreateVocabularyGroup.setOnClickListener {
            findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToNewVocabularyGroupFragment(null, NewVocabularyGroupFragment.MODE_CREATE))
        }

        // File

        binding.buttonImportFileStart.apply {
            setOnClickListener {
                if (checkPermissionToReadFiles())
                    resultLauncherFilePicker.launch(chooser)
                else
                    requestPermissionToReadFiles()

            }
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
                                Toast.makeText(requireContext(), "Failure - Wait for a while and try again", Toast.LENGTH_LONG).show()
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
                // Toast.makeText(requireContext(), getString(R.string.download_started), Toast.LENGTH_SHORT).show()
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

    private fun requestPermissionToReadFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent().apply {
                action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                data = Uri.fromParts("package", requireActivity().packageName, null)
            }
            startActivity(intent)
        } else {
            // below android 11
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    // DO NOT RUN ON UI THREAD
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
                findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToNewVocabularyGroupFragment(importer.vocabularyGroup!!.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT))
            }
            Importer.IMPORT_SUCCESSFULLY_LESSON -> {
                Toast.makeText(context, R.string.import_lesson_successful, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            Importer.IMPORT_WRONG_OR_NONE_TYPE, Importer.IMPORT_NO_JSON -> {
                Toast.makeText(requireContext(), getString(R.string.err_import_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Check if has necessary permissions to load file
     * @return true if has permission, otherwise false
     */
    private fun checkPermissionToReadFiles(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val result1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}