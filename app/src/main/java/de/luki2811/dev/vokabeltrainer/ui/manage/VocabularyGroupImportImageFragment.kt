package de.luki2811.dev.vokabeltrainer.ui.manage

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.set
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.WordTranslation
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreateVocabularyGroupWithImageInfoBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class VocabularyGroupImportImageFragment : Fragment() {

    private var _binding: FragmentCreateVocabularyGroupWithImageInfoBinding? = null
    private val binding get() = _binding!!

    private var name = ""
    private var xCut = 10

    private lateinit var uriOfPhoto: Uri

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                Log.d("PhotoPicker", "Selected URI: $uri")
                checkAndContinue(InputImage.fromFilePath(requireContext(), uri))
            }catch (e: IOException){
                e.printStackTrace()
                binding.buttonCreateVocabularyGroupFromPictureStart.isEnabled = true
                Toast.makeText(requireContext(), getString(R.string.err_could_not_load_image), Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success  ->
        if(success){
            try {
                checkAndContinue(InputImage.fromFilePath(requireContext(), uriOfPhoto))
                binding.buttonCreateVocabularyGroupFromPictureStart.isEnabled = true
            }catch (e: RuntimeException){
                e.printStackTrace()
                Toast.makeText(requireContext(), R.string.err_could_not_load_image, Toast.LENGTH_LONG).show()
            }
        }else{
            Log.d("Take Photo", "Returned")
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ granted ->
        if(granted){
            Thread{
                val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
                val cacheFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", requireContext().cacheDir)
                cacheFile.deleteOnExit()
                uriOfPhoto = FileProvider.getUriForFile(requireContext(),  "${requireActivity().packageName}.provider" , cacheFile)
                requireActivity().runOnUiThread {
                    takePhoto.launch(uriOfPhoto)
                }
            }.start()

        } else{ Toast.makeText(requireContext(), R.string.err_no_permission, Toast.LENGTH_LONG).show() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateVocabularyGroupWithImageInfoBinding.inflate(inflater, container, false)

        binding.buttonCreateVocabularyGroupFromPictureStart.isEnabled = false
        binding.sliderCreateVocabularyGroupFromPictureXCut.isEnabled = false
        binding.buttonCreateVocabularyGroupFromPictureXCut.isEnabled = false

        binding.buttonCreateVocabularyGroupFromPictureChooseImage.apply {
            isEnabled = ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable()
            setOnClickListener {
                val mimeType = "image/*"
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
            }
        }

        binding.buttonCreateVocabularyGroupFromImageTakePhoto.apply {
            setOnClickListener {
                if(requireContext().checkCallingOrSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestCameraPermission.launch(CAMERA)
                }
                else{
                    val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
                    uriOfPhoto = FileProvider.getUriForFile(requireContext(),  "${requireActivity().packageName}.provider" , File.createTempFile("JPEG_${timeStamp}_", ".jpg", requireContext().cacheDir))
                    takePhoto.launch(uriOfPhoto)
                }
            }
        }

        binding.radioButtonCreateVocabularyGroupFromPicture1.isChecked = true
        hideXCut()

        binding.radioGroupCreateVocabularyGroupFromPicture.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                binding.radioButtonCreateVocabularyGroupFromPicture1.id -> {
                    hideXCut()
                }
                binding.radioButtonCreateVocabularyGroupFromPicture2.id -> {
                    showXCut()
                }
            }
        }
        return binding.root
    }

    private fun checkAndContinue(imageInput: InputImage){
        var bitmap = imageInput.bitmapInternal!!
        if(bitmap.width < 100 || bitmap.height < 50){
            Toast.makeText(requireContext(), getString(R.string.err_image_too_small), Toast.LENGTH_LONG).show()
            return
        }

        xCut = bitmap.width/2

        binding.sliderCreateVocabularyGroupFromPictureXCut.apply {
            value = xCut.toFloat()
            valueFrom = 10F
            valueTo = bitmap.width - 10F
        }

        binding.buttonCreateVocabularyGroupFromPictureXCut.setOnClickListener {
            xCut = binding.sliderCreateVocabularyGroupFromPictureXCut.value.roundToInt()

            if(binding.sliderCreateVocabularyGroupFromPictureXCut.value.roundToInt() < 3 || binding.sliderCreateVocabularyGroupFromPictureXCut.value.roundToInt() > bitmap.width-3)
                return@setOnClickListener

            bitmap = imageInput.bitmapInternal!!
            binding.imageViewCreateVocabularyGroupFromPicturePreview.setImageBitmap(printXCut(bitmap))
        }

        binding.buttonCreateVocabularyGroupFromPictureStart.setOnClickListener {
            start(imageInput)
        }

        binding.imageViewCreateVocabularyGroupFromPicturePreview.setImageBitmap(printXCut(bitmap))

        binding.buttonCreateVocabularyGroupFromPictureStart.isEnabled = true
        binding.sliderCreateVocabularyGroupFromPictureXCut.isEnabled = true
        binding.buttonCreateVocabularyGroupFromPictureXCut.isEnabled = true
    }

    private fun printXCut(image: Bitmap): Bitmap{
        val newImage = image.copy(Bitmap.Config.ARGB_8888 , true)
        for(y in 0 until image.height){
            for(x in xCut-2 until xCut+2)
                newImage[x, y] = Color.BLUE
        }
        return newImage
    }

    private fun showXCut() {
        binding.textViewCreateVocabularyGroupFromPictureStep3.visibility = View.VISIBLE
        binding.sliderCreateVocabularyGroupFromPictureXCut.visibility = View.VISIBLE
        binding.imageViewCreateVocabularyGroupFromPicturePreview.visibility = View.VISIBLE
        binding.buttonCreateVocabularyGroupFromPictureXCut.visibility = View.VISIBLE
        binding.dividerCreateVocabularyGroupFromPictureStep3.visibility = View.VISIBLE
    }

    private fun hideXCut(){
        binding.textViewCreateVocabularyGroupFromPictureStep3.visibility = View.GONE
        binding.sliderCreateVocabularyGroupFromPictureXCut.visibility = View.GONE
        binding.imageViewCreateVocabularyGroupFromPicturePreview.visibility = View.GONE
        binding.buttonCreateVocabularyGroupFromPictureXCut.visibility = View.GONE
        binding.dividerCreateVocabularyGroupFromPictureStep3.visibility = View.GONE
    }

    private fun start(inputImage: InputImage){
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                createVocabulary(visionText)
            }
            .addOnFailureListener { e ->
                MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_outline_error_24)
                    .setTitle(R.string.err)
                    .setMessage(e.localizedMessage)
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .show()
                e.printStackTrace()
                findNavController().popBackStack()
            }
    }

    private fun createVocabulary(text: Text) {
        val vocabulary = arrayListOf<WordTranslation>()
        val knownWords = arrayListOf<String>()
        val newWords = arrayListOf<String>()
        var isLoadingNewWord = true
        var index = 0

        val hasTitle = binding.switchCreateVocabularyGroupFromPictureTitle.isChecked

        when(binding.radioGroupCreateVocabularyGroupFromPicture.checkedRadioButtonId){
            binding.radioButtonCreateVocabularyGroupFromPicture1.id -> {
                if(hasTitle)
                    index = -1
                text.textBlocks.forEach { block ->
                    if(hasTitle && index == -1){
                        name = block.text
                        index = 0
                    }else{
                        if(isLoadingNewWord){
                            newWords.add(index, block.text)
                        }else{
                            knownWords.add(index, block.text)
                            // Add word
                            vocabulary.add(index, WordTranslation(knownWords[index], Locale.ROOT, newWords[index], Locale.ROOT, true))
                            index += 1
                        }
                        isLoadingNewWord = !isLoadingNewWord
                    }
                }
            }
            binding.radioButtonCreateVocabularyGroupFromPicture2.id -> {
                if(hasTitle){
                    if(text.text.isNotEmpty() || text.textBlocks.isNotEmpty()){
                        name = text.textBlocks[0].lines[0].text
                    }
                }
                try {
                    text.textBlocks.forEach { textBlock ->
                        textBlock.lines.forEach {
                            if(hasTitle && (it.text == text.textBlocks[0].lines[0].text)){
                                Log.i("name", name)
                            }else{
                                if(it.cornerPoints?.get(0)?.x!! < xCut){
                                    newWords.add(it.text)
                                }else if(it.cornerPoints?.get(0)?.x!! >= xCut){
                                    knownWords.add(it.text)
                                }
                            }

                        }
                    }
                }catch (e: NullPointerException){
                    e.printStackTrace()
                }

                Log.e("Size", "New:${newWords.size}; Known:${knownWords.size}")

                if(newWords.size != knownWords.size){
                    Toast.makeText(requireContext(), getString(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
                    return
                }

                for(i in 0 until newWords.size){
                    vocabulary.add(i, WordTranslation(knownWords[i], Locale.ROOT, newWords[i], Locale.ROOT, true))
                }
            }
        }

        if(vocabulary.size < 2){
            Toast.makeText(requireContext(), getString(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
            return
        }

        vocabulary.forEach {
            it.firstWord = autoCorrectWord(it.firstWord)
            it.secondWord = autoCorrectWord(it.secondWord)
        }

        if(!hasTitle)
            name = ""

        addFirstLanguageToVocabulary(vocabulary)
    }

    private fun addFirstLanguageToVocabulary(vocGroup: ArrayList<WordTranslation>){
        val sb = StringBuilder()
        vocGroup.forEach {
            sb.append(it.firstWord).append("; ")
        }
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(sb.toString())
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                   Log.w("IdentifyLanguage","Couldn't identify language for known words")
                }
                vocGroup.forEach {
                    it.firstLanguage = Locale(languageCode)
                }
                addSecondLanguageToVocabulary(vocGroup)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), getString(R.string.err_could_not_identify_known_language), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    private fun addSecondLanguageToVocabulary(vocGroup: ArrayList<WordTranslation>){
        val sb = StringBuilder()
        vocGroup.forEach {
            sb.append(it.secondWord).append("; ")
        }
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(sb.toString())
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Log.w("IdentifyLanguage","Couldn't identify language for new words" )
                }
                vocGroup.forEach {
                    it.secondLanguage = Locale(languageCode)
                }
                createVocabularyGroup(vocGroup)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), getString(R.string.err_could_not_identify_new_language) , Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    private fun autoCorrectWord(toEdit: String): String{
        var word = "$toEdit "
        Log.i("AutoCorrect","\"$word\"")

        if(word.isBlank() || word.isEmpty()){
            return ""
        }

        if(binding.switchCreateVocabularyGroupFromPictureAutoCorrectWords.isChecked){

            if(word.contains('[') && word.contains(']')){
                word = word.removeRange(word.indexOf('[')..word.indexOf(']'))
            }

            if(word.contains('[')){
                val startIndex = word.indexOf('[')
                val endIndex = word.indexOf(' ', startIndex = startIndex)

                word = if(endIndex == -1){
                    word.removeRange(startIndex, word.lastIndex)
                }else{
                    word.removeRange(startIndex, endIndex)
                }
            }

            if(word.contains(']')){
                val endIndex = word.indexOf(']') + 1

                val allWhiteSpaces = arrayListOf<Int>()
                var index = 0
                word.forEach {
                    if(it == ' ')
                        allWhiteSpaces.add(index)
                    index += 1
                }
                var startIndex = -1

                allWhiteSpaces.forEach {
                    if(it >= endIndex)
                        return@forEach
                    if(it > startIndex)
                        startIndex = it
                }

                word = if(startIndex == -1)
                    word.removeRange(0, endIndex)
                else
                    word.removeRange(startIndex, endIndex)
            }

            if(word.contains("B", ignoreCase = false))
                if(word.indexOf("B", ignoreCase = false) != 0)
                    if(word[word.indexOf('B', ignoreCase = false)-1].isLetter() && word[word.indexOf('B', ignoreCase = false)-1].isLowerCase())
                        word = word.replace("B" ,"ß", ignoreCase = false)


            word = word.replace("Cto)", "(to)")

            if(word.first() == '|'){
                if(word.contains("|e") || word.contains("|a")){
                    word = word.replace("|","l")
                }
            }else
                word = word.replace("|", "")

            if(word.trim().isNotEmpty()){
                if(word.trim().last() == '|')
                    word = word.replace("|", "")
            }
        }

        if(binding.switchCreateVocabularyGroupFromPictureDots.isChecked){
            if(word.contains("... "))
                word = word.replace("... ","")
            if(word.contains(".. "))
                word = word.replace(".. ","")
        }

        if(word.contains(".. ") && !word.contains("... "))
            word = word.replace(".. ","...")
        else if(word.contains(". ") && !word.contains(".. ")){
            word = word.replace(". ","")
        }

        word.trim()
        return word
    }

    private fun createVocabularyGroup(vocGroup: ArrayList<WordTranslation>) {
        try {
            val finalVocabularyGroup = VocabularyGroup(
                name = name,
                secondLanguage = vocGroup[0].secondLanguage,
                firstLanguage = vocGroup[0].firstLanguage,
                vocabulary = vocGroup,
                context = requireContext()
            )
            finish(finalVocabularyGroup)
        }catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
            Toast.makeText(requireContext(), getString(R.string.err_could_not_import_vocabulary_group), Toast.LENGTH_LONG).show()
        }
    }

    private fun finish(vocGroup: VocabularyGroup) {
        findNavController().navigate(VocabularyGroupImportImageFragmentDirections.actionGlobalNewVocabularyGroupFragment(keyVocGroup = vocGroup.getAsJson().toString(), keyMode = VocabularyGroupBasicFragment.MODE_IMPORT))
    }
}