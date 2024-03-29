package de.luki2811.dev.vokabeltrainer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.luki2811.dev.vokabeltrainer.Source
import de.luki2811.dev.vokabeltrainer.adapter.ListSourcesAdapter
import de.luki2811.dev.vokabeltrainer.databinding.FragmentSourcesBinding
import java.net.URL

class SourcesFragment : Fragment() {

    private var _binding: FragmentSourcesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSourcesBinding.inflate(inflater, container, false)

        binding.recyclerViewSources.layoutManager = LinearLayoutManager(requireContext())
        binding.topAppBarSources.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val arraySources: ArrayList<Source> = arrayListOf(
            Source("QRCode Kotlin", "3.3.0", Source.TYPE_MIT, URL("https://raw.githubusercontent.com/g0dkar/qrcode-kotlin/main/LICENSE")),
            Source("Android Open Source Project (AOSP)", null, Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("Kotlin", KotlinVersion.CURRENT.toString() ,Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("Material Components for Android", "1.9.0-alpha01", Source.TYPE_APACHE_2_0, URL("https://raw.githubusercontent.com/google/material-design-icons/master/LICENSE")),
            Source("MPAndroidChart", "3.1.0", Source.TYPE_APACHE_2_0, URL("https://raw.githubusercontent.com/PhilJay/MPAndroidChart/master/LICENSE")),
            Source("ML Kit Android", null, Source.TYPE_APACHE_2_0, URL("https://raw.githubusercontent.com/googlecodelabs/mlkit-android/master/LICENSE"))

            /** Source("AndroidX Navigation Fragment ktx - 2.4.1", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Navigation UI ktx - 2.4.1", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Fragment ktx - 1.4.1", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Appcompat - 1.4.1", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX ConstraintLayout - 2.1.3", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Cardview - 1.0.0", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Legacy Support v4 - 1.0.0", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)),
            Source("AndroidX Core ktx - 1.7.0", Source.TYPE_APACHE_2_0, URL(Source.LINK_APACHE_2_0_DEFAULT)), **/
        )

        arraySources.sortWith(compareBy { it.name })
        binding.recyclerViewSources.adapter = ListSourcesAdapter(arraySources)

        return binding.root
    }
}