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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.AppFile.Companion.loadFromFile
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportWithFileBinding
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

    @Deprecated("Deprecated in Java")
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
            } else if (grantResults.isNotEmpty()) {
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data == null) {
                return
            }
            val file = File(RealPathUtil.getRealPath(requireContext(), data.data!!)!!)

            // Create new Vocabulary Group
            try {
                val dataAsJson = JSONObject(loadFromFile(file))
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
                                findNavController().navigate(ImportWithFileFragmentDirections.actionImportFragmentToNewVocabularyGroupFragment(vocabularyGroup.getAsJson().toString(), NewVocabularyGroupFragment.MODE_IMPORT))
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

                                Log.e("Import TEMP", "VocGroup ID: ${vocGroupFromLesson.id.number} (${vocGroupFromLesson.name}) \ni/length ${i+1}/${vocabularyGroups.length()}")

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
                            if(dataAsJson.getJSONObject("settings").getBoolean("useType1")) useTypes.add(Exercise.TYPE_TRANSLATE_TEXT)
                            if(dataAsJson.getJSONObject("settings").getBoolean("useType2")) useTypes.add(Exercise.TYPE_CHOOSE_OF_THREE_WORDS)
                            if(dataAsJson.getJSONObject("settings").getBoolean("useType3")) useTypes.add(Exercise.TYPE_MATCH_FIVE_WORDS)

                            val lesson = Lesson(nameOfLesson, newIdsVocabularyGroups.toTypedArray() , requireContext(), readOutBoth, askOnlyNewWords, useTypes, numberOfExercises = numberOfExercises)
                            lesson.saveInFile()
                            lesson.saveInIndex()

                            Toast.makeText(requireContext(), R.string.import_lesson_successful, Toast.LENGTH_LONG).show()

                            findNavController().navigate(R.id.action_importFragment_pop)

                        } catch (e: JSONException){
                            Toast.makeText(requireContext(), getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                            cancelImportLesson()
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
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