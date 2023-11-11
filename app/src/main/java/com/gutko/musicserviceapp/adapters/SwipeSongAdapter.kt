package com.gutko.musicserviceapp.adapters

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.gutko.musicserviceapp.R
import com.gutko.musicserviceapp.data.entities.Song

class SwipeSongAdapter : BaseSongAdapter(R.layout.list_item) {

    override val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.itemView.apply {

            val title = "${song.title} - ${song.subtitle}"
            findViewById<TextView>(R.id.tvPrimary).text = title

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}