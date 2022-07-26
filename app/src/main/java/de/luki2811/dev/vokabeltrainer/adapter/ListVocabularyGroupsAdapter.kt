package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.ui.create.NewVocabularyGroupFragment
import de.luki2811.dev.vokabeltrainer.ui.manage.ManageVocabularyGroupsFragmentDirections
import de.luki2811.dev.vokabeltrainer.ui.manage.ShowQrCodeBottomSheet
import de.luki2811.dev.vokabeltrainer.ui.practice.CorrectionBottomSheet
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ListVocabularyGroupsAdapter(
    private val dataSet: ArrayList<VocabularyGroup>,
    private val context: Context,
    private val navController: NavController,
    private val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<ListVocabularyGroupsAdapter.ViewHolder>(), Filterable {

    var dataSetFilter = ArrayList<VocabularyGroup>()

    init {
        dataSetFilter = dataSet
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textViewlistVocabularyGroup)
        val buttonShare: ImageButton = view.findViewById(R.id.buttonShareVocabularyGroup)
        val buttonEdit: ImageButton = view.findViewById(R.id.buttonEditVocabularyGroup)
        val buttonShowQrCode: ImageButton = view.findViewById(R.id.buttonShowQrCodeVocabularyGroup)

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

        viewHolder.textView.text = dataSetFilter[position].name
        viewHolder.buttonShare.setOnClickListener { share(position) }

        viewHolder.buttonShowQrCode.setOnClickListener {
            val showQrCodeBottomSheet = ShowQrCodeBottomSheet()

            showQrCodeBottomSheet.arguments = bundleOf("vocabularyGroup" to dataSetFilter[position].getAsJson().toString(), "name" to dataSetFilter[position].name)

            showQrCodeBottomSheet.show(supportFragmentManager, CorrectionBottomSheet.TAG)
        }

        viewHolder.buttonEdit.setOnClickListener {
            navController.navigate(ManageVocabularyGroupsFragmentDirections.actionManageVocabularyGroupsFragmentToNewVocabularyGroupFragment(dataSetFilter[position].getAsJson().toString(), NewVocabularyGroupFragment.MODE_EDIT))
        }
    }

    // Return the size of your dataSetFilter (invoked by the layout manager)
    override fun getItemCount() = dataSetFilter.size

    private fun share(position: Int){
        val vocabularyGroupJson = JSONObject(AppFile.loadFromFile(File(File(context.filesDir, "vocabularyGroups"), "${dataSetFilter[position].id.number}.json")))
        vocabularyGroupJson.put("type", AppFile.TYPE_FILE_VOCABULARY_GROUP)

        File.createTempFile("vocabularyGroupToExport.json", null, context.cacheDir)
        val cacheFile = File(context.cacheDir,"vocabularyGroupToExport.json")
        AppFile.writeInFile(vocabularyGroupJson.toString(), cacheFile)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, context.packageName + ".provider", cacheFile)
        sharingIntent.type = "application/json"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        context.startActivity(Intent.createChooser(sharingIntent, "Vokabelgruppe teilen mit ..."))
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                dataSetFilter = if (charSearch.isEmpty()) {
                    dataSet
                } else {
                    val resultList = ArrayList<VocabularyGroup>()
                    for (row in dataSet) {
                        if (row.name.lowercase(Locale.getDefault()).contains(charSearch.lowercase(
                                Locale.getDefault()))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = dataSetFilter
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                dataSetFilter = results?.values as ArrayList<VocabularyGroup>
                notifyDataSetChanged()

            }
        }
    }

}
