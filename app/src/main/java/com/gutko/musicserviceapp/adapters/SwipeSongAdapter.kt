package com.gutko.musicserviceapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.gutko.musicserviceapp.data.entities.Song
import com.gutko.musicserviceapp.databinding.SwipeItemBinding

class SwipeSongAdapter : BaseSongAdapter<SwipeSongAdapter.SongViewHolder>() {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SwipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        with(holder.binding) {
            tvPrimary.text = "${song.title} - ${song.subtitle}"
            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    class SongViewHolder(val binding: SwipeItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}