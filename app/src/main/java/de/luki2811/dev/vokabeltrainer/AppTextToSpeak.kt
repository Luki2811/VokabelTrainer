package de.luki2811.dev.vokabeltrainer

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.*

class AppTextToSpeak(var textToSpeak: String, var language: Language, val context: Context) : TextToSpeech.OnInitListener {

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

        if(language.name.equals("französisch", ignoreCase = true)){
            textToSpeak = textToSpeak.replace("qc","quelque chose", ignoreCase = true)
            textToSpeak = textToSpeak.replace("qn", "quelqu'un", ignoreCase = true)
            textToSpeak = textToSpeak.replace("f.","féminin", ignoreCase = true)
            textToSpeak = textToSpeak.replace("m.", "masculin", ignoreCase = true)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS && language.getShortName() != null) {

            val result = tts.setLanguage(Locale(language.getShortName()!!))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported! ($result)")
                Toast.makeText(context, context.getText(R.string.err_lang_not_avaible), Toast.LENGTH_SHORT).show()
                return
            }
            else if(settings.readOutVocabularyGeneral){
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