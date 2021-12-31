package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.luki2811.dev.vokabeltrainer.databinding.ActivityCreateNewBinding


class CreateNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}