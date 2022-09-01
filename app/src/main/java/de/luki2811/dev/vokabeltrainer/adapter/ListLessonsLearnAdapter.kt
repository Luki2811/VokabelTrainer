package de.luki2811.dev.vokabeltrainer.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.*
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity
import java.io.File
import java.util.*


class ListLessonsLearnAdapter(
    private val dataSet: ArrayList<Lesson>,
    private val navController: NavController,
    private val context: Context,
    private val activity: Activity
    ) : RecyclerView.Adapter<ListLessonsLearnAdapter.ViewHolder>(), Filterable {

    var dataSetFilter = ArrayList<Lesson>()

    init {
        dataSet.sortWith(compareByDescending<Lesson> { it.isFavorite }.thenBy { it.name })
        dataSetFilter = dataSet
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textCardLessonName: TextView = view.findViewById(R.id.textCardLessonName)
        val buttonCardDelete: MaterialButton = view.findViewById(R.id.buttonCardDelete)
        val buttonCardEdit: MaterialButton = view.findViewById(R.id.buttonCardEdit)
        val buttonCardPracticeLesson: Button = view.findViewById(R.id.buttonCardPracticeLesson)
        val buttonCardShare: MaterialButton = view.findViewById(R.id.buttonCardShare)
        val buttonCardFavorite: MaterialButton = view.findViewById(R.id.buttonCardFavorite)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_lessons_learn, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textCardLessonName.text = dataSetFilter[position].name

        if(dataSetFilter[position].isFavorite) {
            viewHolder.buttonCardFavorite.setIconResource(R.drawable.ic_baseline_star_24)
            viewHolder.buttonCardFavorite.setIconTintResource(R.color.Yellow)
        } else {
            viewHolder.buttonCardFavorite.setIconResource(R.drawable.ic_outline_star_24)
            viewHolder.buttonCardFavorite.setIconTintResource(R.color.Gray)
        }

        viewHolder.buttonCardFavorite.setOnClickListener {
            if(dataSetFilter[position].isFavorite){
                dataSetFilter[position].isFavorite = false
                viewHolder.buttonCardFavorite.setIconResource(R.drawable.ic_outline_star_24)
                viewHolder.buttonCardFavorite.setIconTintResource(R.color.Gray)
            }else{
                dataSetFilter[position].isFavorite = true
                viewHolder.buttonCardFavorite.setIconResource(R.drawable.ic_baseline_star_24)
                viewHolder.buttonCardFavorite.setIconTintResource(R.color.Yellow)

            }
            dataSetFilter[position].saveInFile()
            dataSetFilter.sortWith(compareByDescending<Lesson> { it.isFavorite }.thenBy { it.name })
            notifyDataSetChanged()
        }

        viewHolder.buttonCardEdit.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Blue)))
            setOnClickListener {
                navController.navigate(MobileNavigationDirections.actionGlobalManageLessonFragment(dataSetFilter[position].getAsJson().toString()))
            }
        }

        viewHolder.buttonCardDelete.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Red)))
            setOnClickListener {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.delete_lesson)
                    .setIcon(R.drawable.outline_delete_24)
                    .setMessage(activity.getString(R.string.do_you_really_want_to_delete_lesson, dataSetFilter[position].name))
                    .setPositiveButton(R.string.delete){ _: DialogInterface, _: Int ->
                        val lesson = dataSetFilter[position]
                        var file = File(context.filesDir, "lessons")
                        file.mkdirs()
                        file = File(file, lesson.id.number.toString() + ".json" )

                        lesson.deleteFromIndex()

                        if(!file.delete()){
                            Log.e("Exception", "Couldn't delete ${lesson.id.number}.json (${lesson.name})")
                            return@setPositiveButton
                        }
                        lesson.id.deleteId()

                        Log.i("Info", "Successfully deleted ${lesson.id.number}.json (${lesson.name})")

                        dataSetFilter.removeAt(position)
                        notifyDataSetChanged()
                        notifyItemRemoved(position)
                    }
                    .setNegativeButton(R.string.cancel){ _: DialogInterface, _: Int ->

                    }
                    .show()
            }
        }

        viewHolder.buttonCardPracticeLesson.setOnClickListener {
            activity.startActivity(Intent(context, PracticeActivity::class.java).putExtra("data_lesson", dataSetFilter[position].getAsJson().toString()))
        }

        viewHolder.buttonCardShare.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Orange)))
            setOnClickListener {
                AppFile.writeInFile(dataSetFilter[position].export().toString(), File(context.filesDir,"testFile"))
                share(position)
            }
        }
    }

    private fun share(position: Int){
        File.createTempFile("lessonToExport.json", null, context.cacheDir)
        val cacheFile = File(context.cacheDir,"lessonToExport.json")
        AppFile.writeInFile(dataSetFilter[position].export().toString(), cacheFile)

        val sharingIntent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", cacheFile)
        sharingIntent.type = "application/json"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        val chooser = Intent.createChooser(sharingIntent, "Share with  ...")
        val resInfoList: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= 33) {
            context.packageManager.queryIntentActivities(chooser, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
        }

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

    override fun getFilter(): Filter{
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                dataSetFilter = if (charSearch.isEmpty()) {
                    dataSet
                } else {
                    val resultList = ArrayList<Lesson>()
                    for (row in dataSet) {
                        if (row.name.lowercase(Locale.getDefault()).contains(charSearch.lowercase(Locale.getDefault()))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = dataSetFilter
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val result = results?.values as ArrayList<Lesson>

                result.sortWith(compareByDescending<Lesson> { it.isFavorite }.thenBy { it.name })

                dataSetFilter = result
                notifyDataSetChanged()

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSetFilter.size
}