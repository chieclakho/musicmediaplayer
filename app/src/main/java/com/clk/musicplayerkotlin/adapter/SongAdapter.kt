package com.clk.musicplayerkotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clk.musicplayerkotlin.MediaManager
import com.clk.musicplayerkotlin.R
import com.clk.musicplayerkotlin.model.Song
import com.clk.musicplayerkotlin.view.fragment.MainFragment.Companion.LEVER_IDLE
import com.clk.musicplayerkotlin.view.fragment.MainFragment.Companion.LEVER_PLAY
import java.util.concurrent.TimeUnit

class SongAdapter(private var listSong: ArrayList<Song>, private var context: Context?, private var event: OnClickListener) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private var current: String? = null
    private var currentSong = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount(): Int = listSong.size
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = listSong[position]
        if (current != null) {
            currentSong = current!!.toInt()
            current = null
        }
        if (position == currentSong) {
            holder.tableRow.setBackgroundResource(R.color.purple_500)
            if (MediaManager.getInstance()!!.getState() == 2) {
                holder.ivPlay.setImageLevel(LEVER_PLAY)
            }
        } else {
            holder.tableRow.setBackgroundResource(R.color.alpha)
            holder.ivPlay.setImageLevel(LEVER_IDLE)
        }
        holder.tableRow.setOnClickListener { event.onClick(holder.tableRow) }
        holder.ivSong.setImageResource(R.drawable.ic_music)
        holder.tvIndex.text = String.format("#%s", position + 1)
        holder.tvName.text = song.title
        holder.tvSinger.text = song.artist
        holder.tableRow.tag = song
        val durationInMillis: Long = song.cover.toLong()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(durationInMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        holder.tvTime.text = String.format("%s:%s", minutes, seconds)
    }

    fun upDateUi(currentSong: Int) {
        this.currentSong = currentSong
        notifyItemRangeChanged(0, listSong.size)
    }
    fun setCurrent(current: String?) {
        this.current = current
    }
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTime: TextView = itemView.findViewById(R.id.tv_time)
        var tvIndex: TextView = itemView.findViewById(R.id.tv_index)
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvSinger: TextView = itemView.findViewById(R.id.tv_singer)
        var tableRow: TableRow = itemView.findViewById(R.id.tb_song)
        var ivSong: ImageView = itemView.findViewById(R.id.iv_song)
        var ivPlay: ImageView = itemView.findViewById(R.id.iv_play_song_item)
    }
}