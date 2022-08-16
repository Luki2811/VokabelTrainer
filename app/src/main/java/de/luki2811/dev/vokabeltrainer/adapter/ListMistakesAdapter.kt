package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import java.time.format.DateTimeFormatter

class ListMistakesAdapter(
    private val dataSet: ArrayList<Mistake>,
    private val numberExercisesTotal: Int = -1,
    private val context: Context
    ) : RecyclerView.Adapter<ListMistakesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosition: TextView = view.findViewById(R.id.textViewItemWordPosition)
        val textViewType: TextView = view.findViewById(R.id.textViewItemMistakeTyp)
        val textViewAnsweredWord: TextView = view.findViewById(R.id.textViewItemAnsweredWord)
        val textViewCorrectWord: TextView = view.findViewById(R.id.textViewItemCorrectAskedWord)
        val textViewAskedWord: TextView = view.findViewById(R.id.textViewItemAskedWord)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_mistake, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textViewPosition.text = if(numberExercisesTotal > 0)
            context.getString(R.string.number_voc_of_rest, dataSet[position].position, numberExercisesTotal)
        else
            context.getString(R.string.last_time_wrong, dataSet[position].lastTimeWrong.format(DateTimeFormatter.ofPattern("EEEE, dd. MMMM yyyy")))

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

    override fun getItemCount() = dataSet.size

}