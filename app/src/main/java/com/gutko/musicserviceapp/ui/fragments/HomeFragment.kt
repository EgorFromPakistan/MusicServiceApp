package com.gutko.musicserviceapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.adapters.SongAdapter
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var songAdapter: SongAdapter

    private var allSongsProgressBar: ProgressBar? = null
    private var rvAllSongs: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allSongsProgressBar = view.findViewById(R.id.allSongsProgressBar)
        rvAllSongs = view.findViewById(R.id.rvAllSongs)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        subscribeObservers()

        songAdapter.setOnItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
    }

    private fun setupRecyclerView() = rvAllSongs?.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar?.isVisible = false
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }

                Status.LOADING -> {
                    allSongsProgressBar?.isVisible = true
                }

                Status.ERROR -> Unit
            }
        }
    }
}