package de.luki2811.dev.vokabeltrainer.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Settings
import de.luki2811.dev.vokabeltrainer.ShortForm
import de.luki2811.dev.vokabeltrainer.ui.manage.ShortFormEditorBottomSheet
import java.util.Locale

class ListShortFormAdapter(var dataset: ArrayList<ShortForm>,
                           private val fragmentManager: FragmentManager,
                            private val lifecycleOwner: LifecycleOwner): RecyclerView.Adapter<ListShortFormAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLong: TextView = view.findViewById(R.id.textViewShortFormListLong)
        val textViewShort: TextView = view.findViewById(R.id.textViewShortFormListShort)
        val textViewLanguage: TextView = view.findViewById(R.id.textViewShortFormListLanguage)
        val buttonDelete: MaterialButton = view.findViewById(R.id.buttonShortFormListSave)
        val buttonEdit: MaterialButton = view.findViewById(R.id.buttonShortFormListEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frame_list_item_short_form, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textViewLong.apply {
            text = dataset[position].longForm
        }
        holder.textViewShort.apply {
            text = dataset[position].shortForm
        }
        holder.textViewLanguage.apply {
            text = dataset[position].language.getDisplayLanguage(Settings(context).appLanguage)
        }
        holder.buttonDelete.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Red)))
            setOnClickListener {
                dataset[holder.layoutPosition].deleteInFile(context)
                dataset.removeAt(holder.layoutPosition)
                notifyItemRemoved(holder.layoutPosition)
            }
        }
        holder.buttonEdit.apply {
            setBackgroundColor(MaterialColors.harmonizeWithPrimary(context, context.getColor(R.color.Blue)))
            setOnClickListener {
                val editSheet = ShortFormEditorBottomSheet(dataset[holder.layoutPosition])
                editSheet.show(fragmentManager, ShortFormEditorBottomSheet.TAG)
                fragmentManager.setFragmentResultListener("finishEditShortForm", lifecycleOwner){ _, bundle ->
                    fragmentManager.clearFragmentResult("finishEditShortForm")
                    fragmentManager.clearFragmentResultListener("finishEditShortForm")
                    try {
                        dataset[holder.layoutPosition] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            bundle.getParcelable("result", ShortForm::class.java)!!
                        }else{
                            bundle.getParcelable("result")!!
                        }
                        notifyItemChanged(holder.layoutPosition, null)
                    }catch (e: ArrayIndexOutOfBoundsException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun addNewItem(){
        dataset.add(0, ShortForm("","", Locale.ROOT))
        notifyItemInserted(0)

        val shortFormEditorBottomSheet = ShortFormEditorBottomSheet(dataset[0])
        shortFormEditorBottomSheet.show(fragmentManager, ShortFormEditorBottomSheet.TAG)
        fragmentManager.setFragmentResultListener("finishEditShortForm", lifecycleOwner){ _, bundle ->
            dataset[0] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("result", ShortForm::class.java)!!
            }else{
                bundle.getParcelable("result")!!
            }
            notifyItemChanged(0, null)
            fragmentManager.clearFragmentResultListener("finishEditShortForm")
            fragmentManager.clearFragmentResult("finishEditShortForm")
        }
    }

    override fun getItemCount() = dataset.size

}