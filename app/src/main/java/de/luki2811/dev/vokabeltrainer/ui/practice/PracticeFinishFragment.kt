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

class PracticeFinishFragment : Fragment() {

    private var _binding: FragmentPracticeFinishBinding? = null
    private val binding get() = _binding!!

    private val args: PracticeFinishFragmentArgs by navArgs()
    private var extraXp = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPracticeFinishBinding.inflate(layoutInflater, container, false)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
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


        binding.textViewFinishedPracticeCorrectInPercent.text = "${args.correctInPercent}%"  //getString(R.string.correct_in_percent, args.correctInPercent)

        binding.buttonFinishPractice.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            setXpToStreak()
        }

        // Calculate extra xp
        extraXp = 5 - args.numberOfMistakes
        if(extraXp >= 0){
            binding.textViewFinishedPracticeXp.text = "+${10+extraXp}XP" //getString(R.string.xp_reached, 10, extraXp)
        }else
            binding.textViewFinishedPracticeXp.text = "+10XP" //getString(R.string.xp_reached, 10, 0)

        // Show Streak Progress
        val streakForProgressBar = Streak(requireContext())
        binding.progressBarFinishedLesson.max = streakForProgressBar.xpGoal
        binding.progressBarFinishedLesson.progress = streakForProgressBar.xpToday + 10 + extraXp

        binding.textViewFinishedPracticeStreakXp.text = getString(R.string.streak_have_of_goal, (streakForProgressBar.xpToday + 10 + extraXp), streakForProgressBar.xpGoal )

        return binding.root
    }

    private fun setXpToStreak() {
        // Basic XP (1XP for 1Word)
        val streak = Streak(requireContext())
        streak.xpToday += 10
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