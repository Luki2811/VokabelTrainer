package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R

class ListMistakesAdapter(
    private val dataSet: ArrayList<Mistake>,
    private val numberExercisesTotal: Int,
    private val context: Context
    ) : RecyclerView.Adapter<ListMistakesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosition: TextView = view.findViewById(R.id.textViewItemWordPosition)
        val textViewType: TextView = view.findViewById(R.id.textViewItemMistakeTyp)
        val textViewAnsweredWord: TextView = view.findViewById(R.id.textViewItemAnsweredWord)
        val textViewCorrectWord: TextView = view.findViewById(R.id.textViewItemCorrectAskedWord)
        val textViewAskedWord: TextView = view.findViewById(R.id.textViewItemAskedWord)
    }
    val totalNumberExcercies = 0

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListMistakesAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_mistake, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ListMistakesAdapter.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textViewPosition.text = context.getString(R.string.number_voc_of_rest, dataSet[position].position, numberExercisesTotal)
        val typeAsString = when(dataSet[position].typeOfPractice){
            1 -> context.getString(R.string.type_of_lesson_1)
            2 -> context.getString(R.string.type_of_lesson_2)
            3 -> context.getString(R.string.type_of_lesson_3)
            else -> "-1"
        }
        viewHolder.textViewType.text = context.getString(R.string.type_of_lesson, typeAsString)
        viewHolder.textViewAskedWord.text = if(dataSet[position].word.isKnownWordAskedAsAnswer) dataSet[position].word.newWord else dataSet[position].word.knownWord
        viewHolder.textViewAnsweredWord.text = dataSet[position].wrongAnswer
        viewHolder.textViewCorrectWord.text = if(dataSet[position].word.isKnownWordAskedAsAnswer) dataSet[position].word.knownWord else dataSet[position].word.newWord
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}