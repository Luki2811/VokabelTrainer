package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyGroup
import de.luki2811.dev.vokabeltrainer.ui.create.NewVocabularyGroupFragment
import de.luki2811.dev.vokabeltrainer.ui.manage.EditVocabularyGroupFragment
import de.luki2811.dev.vokabeltrainer.ui.manage.ManageVocabularyGroupsFragmentDirections
import de.luki2811.dev.vokabeltrainer.ui.manage.ShowQrCodeBottomSheet
import de.luki2811.dev.vokabeltrainer.ui.practice.CorrectionBottomSheet
import java.io.File

class ListVocabularyGroupsAdapter(
    private val dataSet: Array<VocabularyGroup>, private val context: Context, private val navController: NavController,private val supportFragmentManager: FragmentManager) : RecyclerView.Adapter<ListVocabularyGroupsAdapter.ViewHolder>() {
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
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textView.text = dataSet[position].name
        viewHolder.buttonShare.setOnClickListener { shareVocabularyGroup(position) }

        viewHolder.buttonShowQrCode.setOnClickListener {
            val showQrCodeBottomSheet = ShowQrCodeBottomSheet()

            showQrCodeBottomSheet.arguments = bundleOf("vocabularyGroup" to dataSet[position].getAsJson().toString())

            showQrCodeBottomSheet.show(supportFragmentManager, CorrectionBottomSheet.TAG)
        }

        viewHolder.buttonEdit.setOnClickListener {
            navController.navigate(ManageVocabularyGroupsFragmentDirections.actionManageVocabularyGroupsFragmentToNewVocabularyGroupFragment(dataSet[position].getAsJson().toString(), NewVocabularyGroupFragment.MODE_EDIT))
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    private fun shareVocabularyGroup(position: Int){
        val sharingIntent = Intent(Intent.ACTION_SEND)
        val file = File(context.filesDir, "vocabularyGroups")
        file.mkdirs()
        val fileUri = FileProvider.getUriForFile(context,
            context.packageName + ".provider",
            File(file , dataSet[position].id.number.toString() + ".json")
        )
        sharingIntent.type = "application/json"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        context.startActivity(Intent.createChooser(sharingIntent, "Lektion teilen mit"))
    }

}
