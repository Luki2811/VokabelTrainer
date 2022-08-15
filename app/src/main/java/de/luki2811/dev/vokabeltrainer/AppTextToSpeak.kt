package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.*

class AppTextToSpeak(private var textToSpeak: String, var language: Locale, val context: Context) : TextToSpeech.OnInitListener {

    val settings = Settings(context)
    private var tts = TextToSpeech(context,this)


    private fun replaceKnownShorts(){
        textToSpeak = textToSpeak.replace("etw","etwas", ignoreCase = true)
        textToSpeak = textToSpeak.replace("etw.","etwas", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sth", "something", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sth.", "something", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sb", "somebody", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sb.", "somebody", ignoreCase = true)
        textToSpeak = textToSpeak.replace("pl", "plural", ignoreCase = true)
        textToSpeak = textToSpeak.replace("pl.", "plural", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sg", "singular", ignoreCase = true)
        textToSpeak = textToSpeak.replace("sg.", "singular", ignoreCase = true)

        if(language == Locale.FRENCH){
            textToSpeak = textToSpeak.replace("qc","quelque chose", ignoreCase = true)
            textToSpeak = textToSpeak.replace("qn", "quelqu'un", ignoreCase = true)
            textToSpeak = textToSpeak.replace("f.","féminin", ignoreCase = true)
            textToSpeak = textToSpeak.replace("m.", "masculin", ignoreCase = true)
        }
    }

    fun shutdown(){
        tts.stop()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        // val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        if (status == TextToSpeech.SUCCESS) {

            val result = tts.setLanguage(language)

            Log.i("TTS", "Language: ${tts.voice.locale.language}|${tts.voice.locale.country}")

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported! ($result)")
                Toast.makeText(context, context.getText(R.string.err_lang_not_available), Toast.LENGTH_SHORT).show()
                return
            }
            else if(settings.readOutVocabularyGeneral){
                /**
               audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)) {
                    Toast.makeText(context, "Speakerphone off", Toast.LENGTH_SHORT)
                } **/
                /**
               audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)) {
                    Toast.makeText(context, "Speakerphone off", Toast.LENGTH_SHORT)
                } **/
                replaceKnownShorts()
                tts.speak(
                    textToSpeak ,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }
}