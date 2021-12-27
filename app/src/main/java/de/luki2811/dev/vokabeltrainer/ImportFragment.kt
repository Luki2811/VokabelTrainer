package de.luki2811.dev.vokabeltrainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.databinding.FragmentImportBinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class ImportFragment : Fragment() {

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!

    private lateinit var lesson: Lesson

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportBinding.inflate(inflater, container, false)

        val _context = requireContext()

        binding.buttonImportLessonCancel.setOnClickListener { cancelImportLesson() }
        binding.buttonImportLessonFinish.setOnClickListener { finishImport() }


        val uri: Uri? = arguments?.getParcelable("KEY_DATA")
        if(uri == null){
            Toast.makeText(_context, getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_importFragment_to_createNewMainFragment)
        }else{
            val file = File(RealPathUtil.getRealPath(_context, uri)!!)
            val datei = AppFile(file.name)
            // Create new Lesson
            try {
                val lessonAsJSON = JSONObject(datei.loadFromFile(requireContext()))
                if(lessonAsJSON.getJSONArray("vocabulary").length() >= 10){
                    lesson = Lesson(lessonAsJSON)
                    binding.EditLessonNameImport.setText(lesson.name)
                    binding.EditLessonNameImport.hint = lesson.name
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(_context, getText(R.string.err_could_not_import_lesson), Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_importFragment_to_createNewMainFragment)
            }
        }
        return binding.root
    }

    private fun finishImport() {
        val _context = requireContext()
        // Set Text
        val editTextName = binding.EditLessonNameImport
        if(editTextName.text.toString().trim().isNotEmpty())
            lesson.name = editTextName.text.toString()
        // Check if lesson is correct
        if(!lesson.isNameValid(_context))
            return

        val indexFile = File(_context.filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        val indexDatei = AppFile(indexFile.name)

        // Create index
        if (!indexFile.exists()) {
            indexDatei.writeInFile("", _context)
        }
        var indexAsJson: JSONObject
        var jsonArray: JSONArray? = null
        if (indexFile.exists()) {
            try {
                indexAsJson = JSONObject(indexDatei.loadFromFile(_context))
                jsonArray = indexAsJson.getJSONArray("index")
            } catch (e: JSONException) {
                e.printStackTrace()
                indexAsJson = JSONObject()
            }
        } else indexAsJson = JSONObject()
        try {
            if (jsonArray == null) jsonArray = JSONArray()
            val jo = JSONObject()
            jo.put("name", lesson.name)
            jo.put("file", lesson.name + ".json")
            jsonArray.put(jo)
            indexAsJson.put("index", jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        indexDatei.writeInFile(indexAsJson.toString(), _context)

        // Save lesson as .json
        val saveDatei = AppFile(lesson.name + ".json")
        saveDatei.writeInFile(lesson.lessonAsJson.toString(), _context)
        Toast.makeText(_context, getString(R.string.import_lesson_successful), Toast.LENGTH_LONG).show()
        startActivity(
            Intent(_context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun cancelImportLesson(){
        findNavController().navigate(R.id.action_importFragment_to_createNewMainFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}