package de.luki2811.dev.vokabeltrainer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.VocabularyWord

class ListMistakesAdapter(val dataSet: Array<Mistake>,


                          ): RecyclerView.Adapter<ListMistakesAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewPosition: TextView = view.findViewById(R.id.textViewItemWordPosition)
        val textViewType: TextView = view.findViewById(R.id.textViewItemMistakeTyp)
        val textViewAnsweredWord: TextView = view.findViewById(R.id.textViewItemAnsweredWord)
        val textViewCorrectWord: TextView = view.findViewById(R.id.textViewItemCorrectAskedWord)
        val textViewAskedWord: TextView = view.findViewById(R.id.textViewItemAskedWord)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListMistakesAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_sources, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ListMistakesAdapter.ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textViewPosition.text = dataSet[position]
        viewHolder.textViewType.text = dataSet[position].type
        viewHolder.textViewAskedWord.text = dataSet[position].link.toString()
        viewHolder.textViewAnsweredWord.text
        viewHolder.textViewCorrectWord.text

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}