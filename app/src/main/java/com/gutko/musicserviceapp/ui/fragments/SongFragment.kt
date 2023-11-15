package com.gutko.musicserviceapp.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.google.android.material.textview.MaterialTextView
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.exoplayer.isPlaying
import com.gutko.musicserviceapp.exoplayer.toSong
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import com.gutko.musicserviceapp.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    private var tvSongName: MaterialTextView? = null
    private var ivSongImage: ImageView? = null
    private var seekBar: SeekBar? = null
    private var ivSkipPrevious: ImageView? = null
    private var ivSkip: ImageView? = null
    private var ivPlayPauseDetail: ImageView? = null
    private var tvCurTime: MaterialTextView? = null
    private var tvSongDuration: MaterialTextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvSongName = view.findViewById(R.id.tvSongName)
        ivSongImage = view.findViewById(R.id.ivSongImage)
        seekBar = view.findViewById(R.id.seekBar)
        ivSkipPrevious = view.findViewById<ImageView?>(R.id.ivSkipPrevious).apply {
            setOnClickListener {
                mainViewModel.skipToPreviousSong()
            }
        }
        ivSkip = view.findViewById<ImageView?>(R.id.ivSkip).apply {
            setOnClickListener {
                mainViewModel.skipToNextSong()
            }
        }
        ivPlayPauseDetail = view.findViewById<ImageView?>(R.id.ivPlayPauseDetail).apply {
            setOnClickListener {
                curPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }
        }
        tvCurTime = view.findViewById(R.id.tvCurTime)
        tvSongDuration = view.findViewById(R.id.tvSongDuration)

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        subscribeToObserves()

        seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekBar = true
                }
            }
        })
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
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetail?.setImageResource(
                if (playbackState?.isPlaying == true) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
            seekBar?.progress = it?.position?.toInt() ?: 0
        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                seekBar?.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar?.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration?.text = dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime?.text = dateFormat.format(ms)
    }
}