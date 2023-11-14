package com.gutko.musicserviceapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.adapters.SwipeSongAdapter
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.exoplayer.isPlaying
import com.gutko.musicserviceapp.exoplayer.toSong
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var vpSong: ViewPager2? = null
    private var ivCurSongImage: ImageView? = null
    private var ivPlayPause: ImageView? = null

    private var playBackState: PlaybackStateCompat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        vpSong = findViewById<ViewPager2>(R.id.vpSong).apply {
            adapter = swipeSongAdapter
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (playBackState?.isPlaying == true) {
                        mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                    } else {
                        curPlayingSong = swipeSongAdapter.songs[position]
                    }
                }
            })
        }
        ivCurSongImage = findViewById(R.id.ivCurSongImage)
        ivPlayPause = findViewById<ImageView?>(R.id.ivPlayPause)?.apply {
            setOnClickListener {
                curPlayingSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        swipeSongAdapter.songs = songs
                        if (songs.isNotEmpty()) {
                            ivCurSongImage?.let { imageView ->
                                glide.load(curPlayingSong ?: songs[0]).into(imageView)
                            }
                        }
                        switchViewToCurrentSong(curPlayingSong ?: return@observe)
                    }
                }

                Status.LOADING -> Unit
                Status.ERROR -> Unit
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            ivCurSongImage?.let { imageView ->
                glide.load(curPlayingSong?.imageUrl).into(imageView)
            }
            switchViewToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playBackState = it
            ivPlayPause?.setImageResource(
                if (playBackState?.isPlaying == true) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            )
        }
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        Snackbar.make(
                            findViewById(R.id.rootLayout),
                            result.message ?: "An unknown connection error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> {
                        Snackbar.make(
                            findViewById(R.id.rootLayout),
                            result.message ?: "An unknown network error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun switchViewToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            vpSong?.currentItem = newItemIndex
            curPlayingSong = song
        }
    }
}