package de.luki2811.dev.vokabeltrainer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.ui.manage.*
import org.json.JSONObject
import java.io.File
import java.util.*

class ListVocabularyGroupsAdapter(
    private val dataSet: ArrayList<VocabularyGroup>,
    private val context: Context,
    private val navController: NavController,
    private val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<ListVocabularyGroupsAdapter.ViewHolder>(), Filterable {

    var dataSetFilter = ArrayList<VocabularyGroup>()

    init {
        dataSet.sortWith(compareBy { it.name })
        dataSetFilter = dataSet
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textViewText: TextView = view.findViewById(R.id.textViewlistVocabularyGroup)
        val textViewSecondLanguage: TextView = view.findViewById(R.id.textViewSecondLanguage)
        val textViewFirstLanguage: TextView = view.findViewById(R.id.textViewFirstLanguage)
        val buttonShare: MaterialButton = view.findViewById(R.id.buttonShareVocabularyGroup)
        val buttonEdit: MaterialButton = view.findViewById(R.id.buttonEditVocabularyGroup)
        val buttonShowQrCode: MaterialButton = view.findViewById(R.id.buttonShowQrCodeVocabularyGroup)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_vocabulary_group, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataSetFilter at this position and replace the
        // contents of the view with that element

        viewHolder.textViewText.text = dataSetFilter[position].name
        viewHolder.textViewSecondLanguage.text = dataSetFilter[position].mainLanguage.getDisplayLanguage(Settings(context).appLanguage)
        viewHolder.textViewFirstLanguage.text = dataSetFilter[position].otherLanguage.getDisplayLanguage(Settings(context).appLanguage)
        viewHolder.buttonShare.apply {
            setOnClickListener { share(position) }
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Orange)))
        }

        viewHolder.buttonShowQrCode.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Green)))
            setOnClickListener {
                val qrCodeBottomSheet = QrCodeBottomSheet(dataSetFilter[position].exportShort(), dataSetFilter[position].name)
                qrCodeBottomSheet.show(supportFragmentManager, QrCodeBottomSheet.TAG)
            }
        }

        viewHolder.buttonEdit.apply {
            setOnClickListener {
                navController.navigate(VocabularyGroupListFragmentDirections.actionManageVocabularyGroupsFragmentToNewVocabularyGroupFragment(dataSetFilter[position], VocabularyGroupBasicFragment.MODE_EDIT))
            }
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Blue)))
        }
    }

    // Return the size of your dataSetFilter (invoked by the layout manager)
    override fun getItemCount() = dataSetFilter.size

    private fun share(position: Int){
        val vocabularyGroupJson = JSONObject(FileUtil.loadFromFile(File(File(context.filesDir, "vocabularyGroups"), "${dataSetFilter[position].id.number}.json")))
        vocabularyGroupJson.put("type", Exportable.TYPE_VOCABULARY_GROUP)

        File.createTempFile(dataSet[position].getShareFileName(), null, context.cacheDir)
        val cacheFile = File(context.cacheDir,dataSet[position].getShareFileName())
        FileUtil.writeInFile(vocabularyGroupJson.toString(), cacheFile)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, context.packageName + ".provider", cacheFile)
        sharingIntent.type = "application/json"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        val chooser = Intent.createChooser(sharingIntent, null)
        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        context.startActivity(chooser)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString().trim()
                val results = if (charSearch.isEmpty()) {
                    dataSet
                }else if(charSearch.startsWith("word:", ignoreCase = true)) {
                    val searchedWord = charSearch.removePrefix("word:").trim()
                    val resultList = ArrayList<VocabularyGroup>()
                    for (group in dataSet) {
                        if (group.vocabulary.any { vocabularyWord ->
                                vocabularyWord.mainWord.contains(
                                    searchedWord,
                                    ignoreCase = true
                                ) || vocabularyWord.getSecondWordsAsString()
                                    .contains(searchedWord.trim(), ignoreCase = true)
                            })
                            resultList.add(group)
                    }
                    resultList
                }else if(charSearch.startsWith("lang:", ignoreCase = true)){
                    val searchedWord = charSearch.removePrefix("lang:").trim()
                    val resultList = ArrayList<VocabularyGroup>()
                    for(group in dataSet){
                        if(
                            group.mainLanguage.getDisplayLanguage(Settings(context).appLanguage).contains(searchedWord, ignoreCase = true) ||
                            group.mainLanguage.language.equals(searchedWord, ignoreCase = false) ||
                            group.otherLanguage.getDisplayLanguage(Settings(context).appLanguage).contains(searchedWord, ignoreCase = true) ||
                            group.otherLanguage.language.equals(searchedWord, ignoreCase = false)
                        ){
                            resultList.add(group)
                        }
                    }

                    resultList
                }else {
                    val resultList = ArrayList<VocabularyGroup>()
                    for (group in dataSet) {
                        if (group.name.lowercase(Locale.getDefault()).contains(charSearch.lowercase(Locale.getDefault()))) {
                            resultList.add(group)
                        }
                    }
                    resultList
                }

                return FilterResults().apply { this.values = results}
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val result = results?.values as ArrayList<VocabularyGroup>

                result.sortWith(compareBy { it.name })

                dataSetFilter = result

                notifyDataSetChanged()

            }
        }
    }

}
