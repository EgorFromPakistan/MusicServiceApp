package com.gutko.musicserviceapp.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.data.other.Constants.MEDIA_ROOT_ID
import com.gutko.musicserviceapp.data.other.Resource
import com.gutko.musicserviceapp.exoplayer.MusicServiceConnection
import com.gutko.musicserviceapp.exoplayer.isPlayEnabled
import com.gutko.musicserviceapp.exoplayer.isPlaying
import com.gutko.musicserviceapp.exoplayer.isPrepared

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playBackState

    init {
        _mediaItems.postValue(Resource.loading(data = null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            mediaId = it.mediaId!!,
                            title = it.description.title.toString(),
                            subtitle = it.description.subtitle.toString(),
                            songUrl = it.description.mediaUri.toString(),
                            imageUrl = it.description.iconUri.toString()
                        )
                    }
                    _mediaItems.postValue(Resource.success(data = items))
                }
            })
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (toggle) {
                            musicServiceConnection.transportControls.pause()
                        }
                    }

                    playbackState.isPlayEnabled -> {
                        musicServiceConnection.transportControls.play()
                    }

                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}