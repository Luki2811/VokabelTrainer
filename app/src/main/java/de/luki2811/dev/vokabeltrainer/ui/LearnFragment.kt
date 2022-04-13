package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.AppFile
import de.luki2811.dev.vokabeltrainer.Lesson
import de.luki2811.dev.vokabeltrainer.adapter.ListLessonsLearnAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentLearnBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class LearnFragment : Fragment() {

    private var _binding: FragmentLearnBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ListLessonsLearnAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLearnBinding.inflate(inflater, container, false)

        binding.listOfLessonsCards.layoutManager = LinearLayoutManager(requireContext())

        val indexAsJson = JSONObject(AppFile(AppFile.NAME_FILE_INDEX_LESSONS).loadFromFile(requireContext()))
        val arrayList = ArrayList<Lesson>()

        try {
            for(i in 0 until indexAsJson.getJSONArray("index").length()){
                var file = File(requireContext().filesDir, "lessons")
                file.mkdirs()
                file = File(file, indexAsJson.getJSONArray("index").getJSONObject(i).getInt("id").toString() + ".json" )
                val jsonOfVocGroup = JSONObject(AppFile.loadFromFile(file))
                arrayList.add(Lesson(jsonOfVocGroup, requireContext()))
            }
        }catch (e: JSONException){
            e.printStackTrace()
        }
        adapter = ListLessonsLearnAdapter(arrayList, findNavController(), requireContext(), requireActivity())

        binding.listOfLessonsCards.adapter = adapter

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.getFilter().filter(newText)
                return false
            }
        })

        /** Ersetzt durch RecyclerView

        val indexFile = File(requireActivity().filesDir, AppFile.NAME_FILE_INDEX_LESSONS)
        if (indexFile.exists()) {
            val indexDatei = AppFile(AppFile.NAME_FILE_INDEX_LESSONS)
            try {
                val indexJson = JSONObject(indexDatei.loadFromFile(context))
                val indexArrayJson = indexJson.getJSONArray("index")
                val layout = binding.cardsLayoutHome
                for (i in 0 until indexArrayJson.length()) {
                    val layoutparams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutparams.bottomMargin = 5
                    layoutparams.rightMargin = 25
                    layoutparams.leftMargin = 25
                    layoutparams.topMargin = 25
                    val cardView = MaterialCardView(context)
                    cardView.layoutParams = layoutparams
                    cardView.radius = 25f
                    cardView.setContentPadding(10, 10, 10, 10)
                    cardView.setCardBackgroundColor(Color.WHITE)
                    cardView.cardElevation = 3f
                    cardView.maxCardElevation = 5f
                    layout.addView(cardView)

                    // TextView Name
                    val textInCard = TextView(context)
                    val layoutparamsText = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    textInCard.setPadding(10, 10, 10, 10)
                    textInCard.layoutParams = layoutparamsText
                    try {
                        textInCard.text = indexArrayJson.getJSONObject(i).getString("name")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    textInCard.setTextColor(Color.BLACK)
                    textInCard.textSize = 18F

                    // H+W for icons
                    val layoutparamsIcons = RelativeLayout.LayoutParams(100, 100)

                    // Delete button
                    val delete = ImageButton(context)
                    delete.id = View.generateViewId()
                    delete.setBackgroundResource(R.drawable.rounded_red_button)
                    delete.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.outline_delete_24
                        )
                    )
                    delete.layoutParams = layoutparamsIcons
                    delete.setPadding(10, 10, 10, 10)
                    delete.setOnClickListener { view: View? ->
                        MaterialAlertDialogBuilder(context)
                            .setTitle("")
                            .setMessage("Möchtest du wirklich die Lektion löschen ??")
                            .setPositiveButton(R.string.delete) { _: DialogInterface, _: Int ->
                                val file = File(
                                    context.filesDir,
                                    textInCard.text.toString() + ".json"
                                )
                                if (file.exists()) {
                                    val deleted = file.delete()
                                    if (deleted) {
                                        try {
                                            for (i2 in 0 until indexArrayJson.length()) {
                                                if (indexArrayJson.getJSONObject(i2).getString("name").contentEquals(textInCard.text)
                                                ) {
                                                    indexArrayJson.remove(i2)
                                                    indexJson.put("index", indexArrayJson)
                                                    indexDatei.writeInFile(
                                                        indexJson.toString(),
                                                        context
                                                    )
                                                    cardView.visibility = View.INVISIBLE
                                                    cardView.layoutParams =
                                                        LinearLayout.LayoutParams(0, 0)
                                                    Toast.makeText(
                                                        context,
                                                        getString(R.string.deleted_succesfull),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                            .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                                Toast.makeText(
                                    context,
                                    getString(R.string.cancel),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .show()
                    }
                    // Edit Button
                    val edit = ImageButton(context)
                    delete.id = View.generateViewId()
                    edit.setBackgroundResource(R.drawable.rounded_blue_button)
                    edit.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_outline_edit_24
                        )
                    )
                    edit.layoutParams = layoutparamsIcons
                    edit.setOnClickListener {
                        val intent = Intent(context, EditLessonActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }
                    // Export Button
                    /** val export = ImageButton(context)
                    export.id = View.generateViewId()
                    export.setBackgroundResource(R.drawable.rounded_orange_button)
                    export.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_baseline_share_24
                        )
                    )
                    export.layoutParams = layoutparamsIcons
                    export.setOnClickListener {
                        val sharingIntent = Intent(Intent.ACTION_SEND)
                        val fileUri = FileProvider.getUriForFile(
                            context, this.requireContext().packageName + ".provider", File(
                                context.filesDir, textInCard.text.toString() + ".json"
                            )
                        )
                        sharingIntent.type = "application/json"
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
                        startActivity(Intent.createChooser(sharingIntent, "Lektion teilen mit"))
                    } **/


                    // Practice Button
                    val cardLearnButton = MaterialButton(context, null,
                        R.attr.materialButtonOutlinedStyle
                    )
                    cardLearnButton.setText(R.string.practice)
                    cardLearnButton.setTextColor(getColor(context, R.color.Black))
                    cardLearnButton.cornerRadius = 100
                    cardLearnButton.setOnClickListener {
                        val intent = Intent(context, PracticeVocabularyActivity::class.java)
                        intent.putExtra(LEKTION_NAME, textInCard.text)
                        startActivity(intent)
                    }

                    // Add all to several Layouts

                    val cardLayout = LinearLayout(context)
                    cardLayout.orientation = LinearLayout.VERTICAL

                    val deleteIconLayout = LinearLayout(context)
                    deleteIconLayout.addView(delete)
                    deleteIconLayout.setPadding(0,0,7,0)
                    val editIconLayout = LinearLayout(context)
                    editIconLayout.addView(edit)
                    editIconLayout.setPadding(0,0,7,0)
                    val exportIconLayout = LinearLayout(context)
                    // exportIconLayout.addView(export)
                    exportIconLayout.setPadding(0,0,7,0)

                    val iconsLayout = LinearLayout(context)
                    iconsLayout.addView(exportIconLayout)
                    iconsLayout.addView(editIconLayout)
                    iconsLayout.addView(deleteIconLayout)
                    iconsLayout.gravity = Gravity.END

                    val textLayout = LinearLayout(context)
                    textLayout.setPadding(5)

                    val buttonLayout = LinearLayout(context)
                    buttonLayout.gravity = Gravity.CENTER_HORIZONTAL

                    buttonLayout.addView(cardLearnButton)

                    textLayout.addView(textInCard)

                    cardLayout.addView(iconsLayout)
                    cardLayout.addView(textLayout)
                    cardLayout.addView(buttonLayout)
                    cardView.addView(cardLayout)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }**/

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}