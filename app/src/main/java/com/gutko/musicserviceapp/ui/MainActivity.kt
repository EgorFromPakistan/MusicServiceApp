package com.gutko.musicserviceapp.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.adapters.SwipeSongAdapter
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Status
import com.gutko.musicserviceapp.databinding.ActivityMainBinding
import com.gutko.musicserviceapp.exoplayer.isPlaying
import com.gutko.musicserviceapp.exoplayer.toSong
import com.gutko.musicserviceapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playBackState: PlaybackStateCompat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)

        with(binding) {
            vpSong.apply {
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
            ivPlayPause.apply {
                setOnClickListener {
                    curPlayingSong?.let {
                        mainViewModel.playOrToggleSong(it, true)
                    }
                }
            }
            navHostFragment?.findNavController()
                ?.addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.songFragment -> {
                            hideBottomBar()
                        }

                        R.id.homeFragment -> {
                            showBottomBAr()
                        }

                        else -> {
                            showBottomBAr()
                        }
                    }
                }
            swipeSongAdapter.setItemClickListener {
                navHostFragment?.findNavController()?.navigate(R.id.globalActionToSongFragment)
            }
        }
    }

    private fun ActivityMainBinding.hideBottomBar() {
        ivCurSongImage.isVisible = false
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun ActivityMainBinding.showBottomBAr() {
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        swipeSongAdapter.songs = songs
                        if (songs.isNotEmpty()) {
                            binding.ivCurSongImage.let { imageView ->
                                glide.load(curPlayingSong ?: songs[0]).into(imageView)
                            }
                        }
                        binding.switchViewToCurrentSong(curPlayingSong ?: return@observe)
                    }
                }

                Status.LOADING -> Unit
                Status.ERROR -> Unit
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            binding.ivCurSongImage.let { imageView ->
                glide.load(curPlayingSong?.imageUrl).into(imageView)
            }
            binding.switchViewToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) {
            playBackState = it
            binding.ivPlayPause.setImageResource(
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

    private fun ActivityMainBinding.switchViewToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }
}