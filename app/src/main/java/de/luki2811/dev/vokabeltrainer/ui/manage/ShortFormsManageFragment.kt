package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.Exportable
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.ShortForm
import de.luki2811.dev.vokabeltrainer.adapter.ListShortFormAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentShortFormsManageBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class ShortFormsManageFragment : Fragment() {
    private var _binding: FragmentShortFormsManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var shortFormAdapter: ListShortFormAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShortFormsManageBinding.inflate(inflater, container, false)
        shortFormAdapter = ListShortFormAdapter(ShortForm.loadAllShortForms(requireContext()), requireActivity().supportFragmentManager, this)

        binding.bottomAppBar.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.buttonAddNewShortForm -> {
                        binding.recyclerViewManageShortForms.apply {
                            shortFormAdapter.addNewItem()
                            scrollToPosition(0)
                        }
                        true
                    }
                    R.id.buttonShareShortForms -> {
                        shareAll()
                        true
                    }
                    R.id.buttonImportShortForms -> {
                        findNavController().navigate(ShortFormsManageFragmentDirections.actionGlobalCreateNewMainFragment())
                        true
                    }
                    else -> false
                }
            }
        }

        binding.buttonSaveShortFormes.apply {
            setOnClickListener {
                val emptyShortForm = ShortForm("","", Locale.ROOT)

                while (shortFormAdapter.dataset.contains(emptyShortForm)){
                    shortFormAdapter.notifyItemRemoved(shortFormAdapter.dataset.indexOf(emptyShortForm))
                    shortFormAdapter.dataset.removeAt(shortFormAdapter.dataset.indexOf(emptyShortForm))
                }

                shortFormAdapter.dataset.forEach {
                    if(it.shortForm.isBlank() || it.longForm.isBlank() || it.language == Locale.ROOT){
                        Toast.makeText(requireContext(), "One or more fields are blank or incorrect", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }

                ShortForm.setNewShortForms(context, shortFormAdapter.dataset)
                Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
            }
        }

        binding.recyclerViewManageShortForms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shortFormAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }


        return binding.root
    }

    private fun shareAll() {
        val jsonArrayToShow = JSONArray()
        shortFormAdapter.dataset.forEach {
            jsonArrayToShow.put(it.export())
        }

        val qrCodeToShow = QrCodeBottomSheet(JSONObject().put("type", Exportable.TYPE_SHORT_FORM).put("items", jsonArrayToShow).toString(),getString(R.string.all_short_forms))
        qrCodeToShow.show(requireActivity().supportFragmentManager, QrCodeBottomSheet.TAG)
    }
}