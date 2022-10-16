package de.luki2811.dev.vokabeltrainer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExerciseResult(var isCorrect: Boolean, var answer: String ): Parcelable