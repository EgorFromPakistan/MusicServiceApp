package com.gutko.musicserviceapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.google.android.material.textview.MaterialTextView
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.exoplayer.toSong
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import com.gutko.musicserviceapp.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null

    private var tvSongName: MaterialTextView? = null
    private var ivSongImage: ImageView? = null
    private var seekBar: SeekBar? = null
    private var ivSkipPrevious: ImageView? = null
    private var ivSkip: ImageView? = null
    private var ivPlayPauseDetail: ImageView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSongName = view.findViewById(R.id.tvSongName)
        ivSongImage = view.findViewById(R.id.ivSongImage)
        seekBar = view.findViewById(R.id.seekBar)
        ivSkipPrevious = view.findViewById(R.id.ivSkipPrevious)
        ivSkip = view.findViewById(R.id.ivSkip)
        ivPlayPauseDetail = view.findViewById(R.id.ivPlayPauseDetail)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        subscribeToObserves()
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        tvSongName?.text = title
        ivSongImage?.let { glide.load(song.imageUrl).into(it) }
    }

    private fun subscribeToObserves() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        if (curPlayingSong == null && songs.isNotEmpty()) {
                            curPlayingSong = songs[0]
                            updateTitleAndSongImage(songs[0])
                        }
                    }
                }

                else -> Unit
            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }
    }
}