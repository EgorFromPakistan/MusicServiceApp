package com.gutko.musicserviceapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gutko.musicserviceapp.data.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.gutko.musicserviceapp.exoplayer.MusicService
import com.gutko.musicserviceapp.exoplayer.MusicServiceConnection
import com.gutko.musicserviceapp.exoplayer.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playBackState

    private val _curSongDuration = MutableLiveData<Long>()
    private val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    private val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (isActive) {
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos) {
                    pos?.let {
                        _curPlayerPosition.postValue(it)
                    }
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}