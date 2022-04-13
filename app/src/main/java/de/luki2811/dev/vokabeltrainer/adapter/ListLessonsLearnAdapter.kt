package de.luki2811.dev.vokabeltrainer.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.MobileNavigationDirections
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.ui.practice.PracticeActivity
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ListLessonsLearnAdapter(
    private val dataSet: ArrayList<Lesson>,
    private val navController: NavController,
    private val context: Context,
    private val activity: Activity) : RecyclerView.Adapter<ListLessonsLearnAdapter.ViewHolder>(), Filterable {

    var dataSetFilter = ArrayList<Lesson>()

    init {
        dataSetFilter = dataSet
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textCardLanguageKnown: TextView = view.findViewById(R.id.textCardLanguageKnown)
        val textCardLanguageNew: TextView = view.findViewById(R.id.textCardLanguageNew)
        val textCardLessonName: TextView = view.findViewById(R.id.textCardLessonName)
        val buttonCardDelete: ImageButton = view.findViewById(R.id.buttonCardDelete)
        val buttonCardEdit: ImageButton = view.findViewById(R.id.buttonCardEdit)
        val buttonCardPracticeLesson: Button = view.findViewById(R.id.buttonCardPracticeLesson)

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
        viewHolder.textCardLanguageKnown.text = dataSetFilter[position].languageKnow.name
        viewHolder.textCardLanguageNew.text = dataSetFilter[position].languageNew.name

        viewHolder.buttonCardEdit.setOnClickListener {
            navController.navigate(MobileNavigationDirections.actionGlobalManageLessonFragment(dataSetFilter[position].getAsJson().toString()))
        }

        viewHolder.buttonCardDelete.setOnClickListener {
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
                    Toast.makeText(context, context.getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
                }
                .show()
        }
        viewHolder.buttonCardPracticeLesson.setOnClickListener {
            activity.startActivity(Intent(context, PracticeActivity::class.java).putExtra("data_lesson", dataSetFilter[position].getAsJson().toString()))
        }
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

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                dataSetFilter = results?.values as ArrayList<Lesson>
                notifyDataSetChanged()

            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSetFilter.size
}