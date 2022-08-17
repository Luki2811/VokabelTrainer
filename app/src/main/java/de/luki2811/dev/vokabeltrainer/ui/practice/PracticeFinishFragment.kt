package de.luki2811.dev.vokabeltrainer.ui.practice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Streak
import de.luki2811.dev.vokabeltrainer.databinding.FragmentPracticeFinishBinding
import de.luki2811.dev.vokabeltrainer.ui.MainActivity
import java.util.concurrent.TimeUnit
import kotlin.math.round

class PracticeFinishFragment : Fragment() {

    private var _binding: FragmentPracticeFinishBinding? = null
    private val binding get() = _binding!!

    private val args: PracticeFinishFragmentArgs by navArgs()
    private var extraXp = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeFinishBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            setXpToStreak()
            requireActivity().startActivity(Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

        binding.textViewFinishedPracticeTime.text = getFormattedTime()

        // Check if there are mistakes to show or value is correct
        if(args.numberOfMistakes < 0){
            Log.w("Warning","Failed to load number of mistakes or value is incorrect")
            binding.buttonFinishPracticeSeeMistakes.isEnabled = false
        }else
            binding.buttonFinishPracticeSeeMistakes.isEnabled = args.numberOfMistakes != 0

        binding.buttonFinishPracticeSeeMistakes.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult("sendToMistakes", bundleOf("" to ""))
        }

        binding.textViewFinishedPracticeCorrectInPercent.text = getString(R.string.correct_in_percent, args.correctInPercent)

        binding.buttonFinishPractice.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            setXpToStreak()
        }

        // Calculate extra xp
        extraXp = round(args.numberOfWords.toDouble()/2).toInt() - args.numberOfMistakes

        if(extraXp >= 0){
            binding.textViewFinishedPracticeXp.text = getString(R.string.xp_reached, args.numberOfWords+extraXp)
        }else
            binding.textViewFinishedPracticeXp.text = getString(R.string.xp_reached, args.numberOfWords)

        // Show Streak Progress
        val streakForProgressBar = Streak(requireContext())
        streakForProgressBar.xpToday += args.numberOfWords + extraXp
        binding.progressBarFinishedLesson.max = streakForProgressBar.xpGoal
        binding.progressBarFinishedLesson.progress = streakForProgressBar.xpToday

        binding.textViewFinishedPracticeStreakXp.text = getString(R.string.streak_have_of_goal, streakForProgressBar.xpToday, streakForProgressBar.xpGoal)

        binding.textViewFinishedPracticeStreakInfo.text = if (streakForProgressBar.xpToday < streakForProgressBar.xpGoal) getString(
            R.string.streak_left_to_do_for_next_day,
            streakForProgressBar.xpGoal - streakForProgressBar.xpToday,
            streakForProgressBar.lengthInDay + 1
        ) else getString(R.string.streak_reached_goal)

        return binding.root
    }

    private fun setXpToStreak() {
        // Basic XP (1XP for 1Word)
        val streak = Streak(requireContext())
        streak.xpToday += args.numberOfWords
        if(extraXp >= 0) streak.xpToday += extraXp
        streak.refresh()
    }

    private fun getFormattedTime(): String{
        var seconds = args.timeInSeconds.toLong()
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        return "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }
}