package com.gutko.musicserviceapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gutko.musicserviceapp.adapters.SongAdapter
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.databinding.FragmentHomeBinding
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
            setupRecyclerView()
            subscribeObservers()

            songAdapter.setItemClickListener {
                mainViewModel.playOrToggleSong(it)
            }
        }
    }

    private fun FragmentHomeBinding.setupRecyclerView() = rvAllSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun FragmentHomeBinding.subscribeObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }

                Status.LOADING -> {
                    allSongsProgressBar.isVisible = true
                }

                Status.ERROR -> Unit
            }
        }
    }
}