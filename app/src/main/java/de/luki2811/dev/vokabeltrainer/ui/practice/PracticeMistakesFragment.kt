package de.luki2811.dev.vokabeltrainer.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.Mistake
import de.luki2811.dev.vokabeltrainer.adapter.ListMistakesAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeMistakesBinding


class PracticeMistakesFragment : Fragment() {
    private var _binding: FragmentPracticeMistakesBinding? = null
    private val binding get() = _binding!!
    private val args: PracticeMistakesFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeMistakesBinding.inflate(inflater, container, false)

        val mistakes: ArrayList<Mistake> = args.mistakes.toMutableList() as ArrayList<Mistake>

        binding.recyclerViewPracticeMistake.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPracticeMistake.adapter = ListMistakesAdapter(mistakes, args.totalNumberExcercies, requireContext())

        binding.practiceTopAppBarMistake.setNavigationOnClickListener {
            findNavController().popBackStack()
        }


        return binding.root
    }
}