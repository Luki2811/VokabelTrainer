package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.R
import org.w3c.dom.Text
import java.io.File

class ListLessonsLearnAdapter(
    private val dataSet: ArrayList<Lesson>, private val navController: NavController, private val context: Context) : RecyclerView.Adapter<ListLessonsLearnAdapter.ViewHolder>() {
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
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textCardLessonName.text = dataSet[position].name
        viewHolder.textCardLanguageKnown.text = dataSet[position].languageKnow.getName()
        viewHolder.textCardLanguageNew.text = dataSet[position].languageNew.getName()

        viewHolder.buttonCardPracticeLesson.setOnClickListener {
            // navController.navigate(R.id.)
        }

        viewHolder.buttonCardDelete.setOnClickListener {

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_lesson)
                .setMessage(R.string.do_you_really_want_to_delete_lesson)
                .setPositiveButton(R.string.delete){ _: DialogInterface, _: Int ->
                    val lesson = dataSet[position]
                    var file = File(context.filesDir, "lessons")
                    file.mkdirs()
                    file = File(file, lesson.id.number.toString() + ".json" )

                    lesson.deleteFromIndex(context)

                    if(!file.delete()){
                        Log.e("Exception", "Couldn't delete ${lesson.id.number}.json (${lesson.name})")
                        return@setPositiveButton
                    }
                    lesson.id.deleteId()

                    Log.i("Info", "Successfully deleted ${lesson.id.number}.json (${lesson.name})")

                    dataSet.removeAt(position)
                    notifyItemRemoved(position)
                }
                .setNegativeButton(R.string.cancel){ _: DialogInterface, _: Int ->
                    Toast.makeText(context, context.getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // TODO: Üben hinzufügen
        // TODO: Bearbeiten hinzufügen
        // viewHolder.buttonEdit.setOnClickListener { navController.navigate(R.id.action_manageVocabularyGroupsFragment_to_editVocabularyGroupFragment, bundleOf("key_voc_group" to dataSet[position].getAsJson().toString())) }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}