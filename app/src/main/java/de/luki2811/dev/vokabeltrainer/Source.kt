package de.luki2811.dev.vokabeltrainer

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import java.net.URL

class Source(val name: String, val version: String?, val type: String, val link: URL, val other: String = "") {

    companion object{

        fun sendToOssLicensesMenu(activity: Activity, context: Context){
            activity.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            OssLicensesMenuActivity.setActivityTitle(activity.getString(R.string.open_source_licences))
        }


        const val TYPE_MIT = "The MIT License"
        const val TYPE_APACHE_2_0 = "The Apache Software License, Version 2.0"

        const val LINK_APACHE_2_0_DEFAULT = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    }
}