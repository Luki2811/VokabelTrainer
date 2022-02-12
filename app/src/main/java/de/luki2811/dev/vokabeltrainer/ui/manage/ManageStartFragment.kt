package de.luki2811.dev.vokabeltrainer.ui.manage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.NavigationManageArgs
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentManageStartBinding
import de.luki2811.dev.vokabeltrainer.ui.SettingsFragment


class ManageStartFragment : Fragment() {

    private var _binding: FragmentManageStartBinding? = null
    private val binding get() = _binding!!
    private val args:NavigationManageArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageStartBinding.inflate(inflater, container,false)

        Log.i("Fragment","Start Fragment")


        when(args.direction){
            NAV_MANAGE_LANGUAGE -> {
                findNavController().navigate(R.id.action_manageStartFragment_to_manageLanguagesFragment)

            }
            NAV_MANAGE_VOCABULARY_GROUP -> {
                findNavController().navigate(R.id.action_manageStartFragment_to_manageVocabularyGroupsFragment)

            }
            NAV_LEAVE -> findNavController().navigate(R.id.action_manageStartFragment_to_navigationMain)
            else -> {
                Toast.makeText(requireContext(), "Fehler", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_manageStartFragment_to_navigationMain)

            }
        }

        return binding.root
    }

    companion object{
        const val NAV_LEAVE = 0
        const val NAV_MANAGE_LANGUAGE = 1
        const val NAV_MANAGE_VOCABULARY_GROUP = 2
    }

}