package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Streak
import de.luki2811.dev.vokabeltrainer.databinding.FragmentStreakBinding

class StreakFragment : Fragment() {

    private var _binding: FragmentStreakBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStreakBinding.inflate(inflater, container, false)

        // set Streak
        val streak = Streak(requireContext())

        binding.progressBarFinishedLesson.max = streak.xpGoal
        binding.progressBarFinishedLesson.progress = streak.xpToday
        binding.textViewMainProgressStreak.text = getString(R.string.streak_have_of_goal, streak.xpToday, streak.xpGoal)
        if (streak.xpToday < streak.xpGoal) binding.textViewMainStreakBottom.text = getString(
            R.string.streak_left_to_do_for_next_day,
            streak.xpGoal - streak.xpToday,
            streak.lengthInDay + 1
        ) else binding.textViewMainStreakBottom.setText(R.string.streak_reached_goal)

        binding.textViewStreakInDays.text = if(streak.lengthInDay == 1)
            getString(R.string.streak_in_day, 1)
        else
            getString(R.string.streak_in_days, streak.lengthInDay)

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}