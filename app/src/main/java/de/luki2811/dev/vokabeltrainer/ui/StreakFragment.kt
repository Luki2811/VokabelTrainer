package de.luki2811.dev.vokabeltrainer.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.color.MaterialColors
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.Streak
import de.luki2811.dev.vokabeltrainer.databinding.FragmentStreakBinding
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class StreakFragment : Fragment() {

    private var _binding: FragmentStreakBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        setupLineChart()

        return binding.root
    }

    private fun setupLineChart(){
        val entries = ArrayList<Entry>()
        val streak = Streak(requireContext())
        var index = 1f
        streak.allDaysXp.forEach {
            entries.add(Entry(index, it.second.toFloat()))
            index += 1
        }

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.apply {
            setDrawValues(false)
            setDrawFilled(true)
            circleRadius = 5f
            // circleHoleColor = MaterialColors.getColor(requireContext(), R.attr.colorPrimary, Color.BLACK)
            setCircleColor(MaterialColors.getColor(requireContext(), R.attr.colorPrimary, Color.BLACK))
            color = MaterialColors.getColor(requireContext(), R.attr.colorPrimary, Color.BLACK)
            lineWidth = 3f
            fillColor = MaterialColors.getColor(requireContext(), R.attr.colorPrimaryContainer, Color.BLACK)
            // fillAlpha = requireActivity().getColor(R.color.Red)
        }


        binding.lineChartStreak.apply {
            xAxis.apply {
                textColor = MaterialColors.getColor(requireContext(), R.attr.colorPrimary, Color.BLACK)
                position = XAxis.XAxisPosition.BOTTOM
                labelRotationAngle = 0f
                axisMinimum = 1f
                axisMaximum = 7f
                valueFormatter = LineChartXAxisValueFormatter()
            }
            axisLeft.apply {
                textColor = MaterialColors.getColor(requireContext(), R.attr.colorPrimary, Color.BLACK)
                axisMinimum = 0f
                axisMaximum = streak.allDaysXp.filterIndexed { index, _ -> index < 7 }.maxOf { it.second }.toFloat() + 10
            }
            legend.isEnabled = false
            description.isEnabled = false
            data = LineData(lineDataSet)
            data.isHighlightEnabled = false

            axisRight.isEnabled = false
            isScaleXEnabled = false
            isScaleYEnabled = true
            isDoubleTapToZoomEnabled = false
            // animateX(500, Easing.EaseInOutBack)
        }

    }

    inner class LineChartXAxisValueFormatter: IndexAxisValueFormatter() {
        val streak = Streak(requireContext())
        override fun getFormattedValue(value: Float): String {
            val formatter = DateTimeFormatter.ofPattern("dd.MM")
           return when(value.roundToInt()){
               1 -> streak.allDaysXp[0].first.format(formatter)
               2 -> if(streak.allDaysXp.size >= 2) streak.allDaysXp[1].first.format(formatter) else ""
               3 -> if(streak.allDaysXp.size >= 3) streak.allDaysXp[2].first.format(formatter) else ""
               4 -> if(streak.allDaysXp.size >= 4) streak.allDaysXp[3].first.format(formatter) else ""
               5 -> if(streak.allDaysXp.size >= 5) streak.allDaysXp[4].first.format(formatter) else ""
               6 -> if(streak.allDaysXp.size >= 6) streak.allDaysXp[5].first.format(formatter) else ""
               7 -> if(streak.allDaysXp.size >= 7) streak.allDaysXp[6].first.format(formatter) else ""
               else -> ""
           }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}