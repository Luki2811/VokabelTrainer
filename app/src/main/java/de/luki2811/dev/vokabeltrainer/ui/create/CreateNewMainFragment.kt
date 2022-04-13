package de.luki2811.dev.vokabeltrainer.ui.create

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.luki2811.dev.vokabeltrainer.R
import de.luki2811.dev.vokabeltrainer.databinding.FragmentCreateNewMainBinding
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class CreateNewMainFragment : Fragment() {

    private var _binding: FragmentCreateNewMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateNewMainBinding.inflate(inflater, container, false)
        binding.importLessonButton.setOnClickListener {
            findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToImportFragment())
        }
        binding.createLessonButton.setOnClickListener {
            findNavController().navigate(R.id.action_createNewMainFragment_to_newLessonFragment)
        }
        binding.createVocabularyGroupButton.setOnClickListener {
            findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToNewVocabularyGroupFragment(null, NewVocabularyGroupFragment.MODE_CREATE))
        }

        binding.buttonLoadFromURL.isEnabled = true

        binding.buttonLoadFromURL.setOnClickListener {
            findNavController().navigate(CreateNewMainFragmentDirections.actionCreateNewMainFragmentToImportWithURLFragment())
        }

        binding.buttonLoadFromQrCode.setOnClickListener {
           findNavController().navigate(R.id.action_createNewMainFragment_to_importWithQrCodeFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}