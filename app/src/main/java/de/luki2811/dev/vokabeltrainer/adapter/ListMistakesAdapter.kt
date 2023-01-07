package de.luki2811.dev.vokabeltrainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyWord
import de.luki2811.dev.vokabeltrainer.WordTranslation
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
        val buttonDelete: MaterialButton = view.findViewById(R.id.buttonMistakeCardDelete)
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
            else -> "-"
        }
        viewHolder.textViewType.text = context.getString(R.string.type_of_lesson, typeAsString)
        when(dataSet[position].word.typeOfWord){
            VocabularyWord.TYPE_TRANSLATION -> {
                viewHolder.textViewAskedWord.text = if(dataSet[position].askedForSecondWord) (dataSet[position].word as WordTranslation).mainWord else (dataSet[position].word as WordTranslation).getSecondWordsAsString()
                viewHolder.textViewCorrectWord.text = if(dataSet[position].askedForSecondWord) (dataSet[position].word as WordTranslation).getSecondWordsAsString() else (dataSet[position].word as WordTranslation).mainWord
            }

        }
        viewHolder.textViewAnsweredWord.text = dataSet[position].wrongAnswer
        viewHolder.buttonDelete.apply {
            setOnClickListener {
                if(viewHolder.layoutPosition >= 0){
                    dataSet[viewHolder.layoutPosition].removeFromFile(context)
                    notifyItemRemoved(viewHolder.layoutPosition)
                }
            }
        }
    }

    override fun getItemCount() = dataSet.size

}
