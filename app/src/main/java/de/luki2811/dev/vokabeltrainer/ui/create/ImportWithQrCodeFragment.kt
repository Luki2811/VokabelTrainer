package de.luki2811.dev.vokabeltrainer.ui.create

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithQrCodeBinding
import org.json.JSONException
import org.json.JSONObject


class ImportWithQrCodeFragment : Fragment() {

    var _binding: FragmentImportWithQrCodeBinding? = null
    val binding get() = _binding!!

    private val CAMERA_REQUEST_CODE = 101

    private lateinit var codeScanner: CodeScanner
    private lateinit var result: String
    private lateinit var vocabularyGroup: VocabularyGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportWithQrCodeBinding.inflate(layoutInflater, container, false)

        binding.buttonFinishImportWithQrCode.isEnabled = false

        setupPermissions()
        codeScanner()

        return binding.root
    }

    private fun codeScanner(){
        codeScanner = CodeScanner(requireContext(), binding.scannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                result = it.text
                requireActivity().runOnUiThread {

                    vocabularyGroup = try {
                        VocabularyGroup(JSONObject(result), context = requireContext())
                    }catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.err_could_not_import_vocabulary_group),
                            Toast.LENGTH_LONG
                        ).show()
                        return@runOnUiThread
                    }
                    if(vocabularyGroup.vocabulary.isEmpty()){
                        Toast.makeText(requireContext(), R.string.err_could_not_import_vocabulary_group, Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }

                    binding.buttonFinishImportWithQrCode.isEnabled = true

                    binding.textViewNameOfImportedVocabularyGroup.text = vocabularyGroup.name
                    binding.buttonFinishImportWithQrCode.setOnClickListener { findNavController().navigate(ImportWithQrCodeFragmentDirections.actionGlobalNewVocabularyGroupFragment(vocabularyGroup.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT)) }
                }

            }
            errorCallback = ErrorCallback {
                requireActivity().runOnUiThread{
                    Log.e("Scanner", "Camera initialization error: ${it.message}")
                }
            }

            binding.scannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    private fun setupPermissions(){
        val permission: Int = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA)

        if(permission != PackageManager.PERMISSION_GRANTED)
            makeRequest()
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Kamera wird zum QR-Code Scannen benötigt !", Toast.LENGTH_LONG).show()
                } else{
                    // Successfully
                }
            }
        }
    }
}