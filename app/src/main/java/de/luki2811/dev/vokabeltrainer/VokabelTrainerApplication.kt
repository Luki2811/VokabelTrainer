package de.luki2811.dev.vokabeltrainer

import android.app.Application
import com.google.android.material.color.DynamicColors

class VokabelTrainerApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        try {
            if(Settings(applicationContext).useDynamicColors)
                DynamicColors.applyToActivitiesIfAvailable(this)

        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}