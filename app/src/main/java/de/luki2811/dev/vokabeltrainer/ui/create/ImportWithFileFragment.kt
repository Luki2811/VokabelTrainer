package de.luki2811.dev.vokabeltrainer.ui.create

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.loadFromFile
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithFileBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class ImportWithFileFragment : Fragment() {

    private var _binding: FragmentImportWithFileBinding? = null
    private val binding get() = _binding!!

    private lateinit var vocabularyGroup: VocabularyGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportWithFileBinding.inflate(inflater, container, false)

        startImport()

        return binding.root
    }

    private var requestCode = 1

    private fun startImport() {
        if (checkPermission()) {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.type = "application/json"
            startActivityForResult(chooseFile, requestCode)
        } else requestPermission()
    }

    private val PERMISSION_REQUEST_CODE = 100

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val result1 = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            val file = File(RealPathUtil.getRealPath(requireContext(), uri!!)!!)

            // Create new Lesson
            try {
                val groupAsJSON = JSONObject(loadFromFile(file))
                if(groupAsJSON.getJSONArray("vocabulary").length() >= 2){
                    vocabularyGroup = VocabularyGroup(groupAsJSON, context = requireContext())
                    findNavController().navigate(ImportWithFileFragmentDirections.actionImportFragmentToNewVocabularyGroupFragment(vocabularyGroup.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), getText(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
                cancelImportLesson()
            }


        } else {
            cancelImportLesson()
        }
    }

    private fun cancelImportLesson(){
        findNavController().navigate(R.id.action_importFragment_pop)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}