package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale


class TextToSpeechUtil(val context: Context) {

    private var isInitialized = false
    private var queuedRequests = ArrayList<Pair<String, Locale>>()

    private var tts: TextToSpeech = TextToSpeech(context) { status ->

        if(status == TextToSpeech.SUCCESS) {
            Log.i("TTS", "Successfully initialize TTS")
            isInitialized = true
            queuedRequests.forEach{
                speak(it.first, it.second)
            }
        }
        else {
            Log.w("TTS", "Failed to initialize TTS")
        }
    }

    fun speak(_text: String, language: Locale): Int{
        var text = _text

        if(!Settings(context).readOutVocabularyGeneral)
            return ERROR_GLOBAL_DEACTIVATED_SPEAKING

        if(!isInitialized){
            queuedRequests.add(text to language)
            return QUEUED_REQUEST
        }

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
            Toast.makeText(context, context.getString(R.string.increase_volume), Toast.LENGTH_SHORT).show()
        }

        when(tts.setLanguage(language)){
            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                Log.w(LOG_TAG, "The Language specified is not supported!")
                // Toast.makeText(context, context.getText(R.string.err_lang_not_available), Toast.LENGTH_SHORT).show()
                return ERROR_MISSING_LANG_DATA
            }
            TextToSpeech.LANG_AVAILABLE, TextToSpeech.LANG_COUNTRY_AVAILABLE, TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE -> {
                text = ShortForm.replaceShortFormsWithLongForms(text, language, context)
                Log.d(LOG_TAG, "Text to speak: $text")
                return when(tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS")){
                    TextToSpeech.SUCCESS -> { SUCCESS }
                    TextToSpeech.ERROR -> {
                        Log.w("TTS", "Error while queuing text")
                        ERROR_UNKNOWN
                    }
                    else -> ERROR_UNKNOWN
                }
            }
        }
        return ERROR_UNKNOWN
    }

    fun finish(){
        tts.stop()
        tts.shutdown()
    }

    companion object{

        const val LOG_TAG = "TTS"

        const val QUEUED_REQUEST = 1
        const val SUCCESS = 0
        const val ERROR_MISSING_LANG_DATA = -1
        const val ERROR_GLOBAL_DEACTIVATED_SPEAKING = -2
        const val ERROR_UNKNOWN = -3


    }
}