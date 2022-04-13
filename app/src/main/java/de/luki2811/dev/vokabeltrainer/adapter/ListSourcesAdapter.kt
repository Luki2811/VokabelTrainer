package de.luki2811.dev.vokabeltrainer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Source
import java.util.jar.Attributes

class ListSourcesAdapter(private val dataSet: ArrayList<Source>) : RecyclerView.Adapter<ListSourcesAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.textViewSourceName)
        val textViewType: TextView = view.findViewById(R.id.textViewSourceType)
        val textViewLink: TextView = view.findViewById(R.id.textViewSourceLink)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.frame_list_item_sources, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.textViewName.text = dataSet[position].name
        viewHolder.textViewType.text = dataSet[position].type
        viewHolder.textViewLink.text = dataSet[position].link.toString()

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size


}
