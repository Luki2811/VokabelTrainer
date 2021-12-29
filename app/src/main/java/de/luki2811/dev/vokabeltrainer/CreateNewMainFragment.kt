package de.luki2811.dev.vokabeltrainer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreateNewMainBinding

class CreateNewMainFragment : Fragment() {

    private var _binding: FragmentCreateNewMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNewMainBinding.inflate(inflater, container, false)
        binding.importLessonButton.setOnClickListener {
            importLesson(it)
        }
        binding.createLessonButton.setOnClickListener {
            findNavController().navigate(R.id.action_createNewMainFragment_to_newLessonFragment)
        }
        binding.createVocabularyGroupButton.setOnClickListener {
            findNavController().navigate(R.id.action_createNewMainFragment_to_newVocabularyGroupFragment)
        }

        // TODO: Fragment hinzufügen, um es mit NFC zu empfangen -> https://developer.android.com/training/beam-files
        binding.getWithNFC.isEnabled = false
        binding.getWithNFC.setOnClickListener { Toast.makeText(requireContext(), "NFC deaktiviert", Toast.LENGTH_LONG).show() }

        return binding.root
    }

    private var requestCode = 1
    private fun importLesson(view: View) {
        if (checkPermission()) {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.type = "application/json"
            startActivityForResult(chooseFile, requestCode)
        } else requestPermission()
    }

    private var PERMISSION_REQUEST_CODE = 100
    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val result1 = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", requireContext().packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                    chooseFile.type = "application/json"
                    startActivityForResult(chooseFile, requestCode)
                } else {
                    Toast.makeText(requireContext(), "Allow permission for storage access!", Toast.LENGTH_SHORT).show()
                }
            } else if (grantResults.size > 0) {
                val READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                    val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
                    chooseFile.type = "application/json"
                    startActivityForResult(chooseFile, requestCode)
                } else {
                    Toast.makeText(requireContext(), "Allow permission for storage access!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data == null) {
                return
            }
            val uri = data.data
            findNavController().navigate(R.id.action_createNewMainFragment_to_importFragment, bundleOf("KEY_DATA" to uri.toString()))

        } else {
            Toast.makeText(requireContext(), getString(R.string.err), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val JSON_OBJECT = "de.luki2811.dev.vokabeltrainer.JSON_Object"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}