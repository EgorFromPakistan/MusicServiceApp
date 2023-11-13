package com.gutko.musicserviceapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.adapters.SwipeSongAdapter
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Status
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        vpSong = findViewById<ViewPager2>(R.id.vpSong).apply {
            adapter = swipeSongAdapter
        }
        ivCurSongImage = findViewById(R.id.ivCurSongImage)
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
    }

    private fun switchViewToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            vpSong?.currentItem = newItemIndex
            curPlayingSong = song
        }
    }
}