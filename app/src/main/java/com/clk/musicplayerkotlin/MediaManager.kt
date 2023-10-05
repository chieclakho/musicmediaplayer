package com.clk.musicplayerkotlin

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.clk.musicplayerkotlin.App.Companion.instance
import com.clk.musicplayerkotlin.model.Song
import com.clk.musicplayerkotlin.view.fragment.MainFragment
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

open class MediaManager {
    companion object {
        private val TAG = MediaManager::class.java.name
        private var player: MediaPlayer? = null
        const val STATE_IDLE = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSED = 3
        var state = STATE_IDLE
        private var instance: MediaManager? = null
        val listSong: ArrayList<Song> = ArrayList()
        var currentSong = 0
        var play: Int = 0
        val liveData = MutableLiveData<Int>()
        val shuffle = MutableLiveData(0)
        var onCompletionevent: OnCompletionListener? = null

        fun getInstance(): MediaManager? {
            if (instance == null) {
                instance = MediaManager()
            }
            return instance
        }
    }

    fun getState(): Int {
        return state
    }

    fun getLiveData(): MutableLiveData<Int> {
        return liveData
    }

    fun getListSong(): ArrayList<Song> {
        return listSong
    }

    fun getShuffle(): MutableLiveData<Int> {
        return shuffle
    }

    init {
        player = MediaPlayer()
        player!!.setOnCompletionListener {
            if (play != 0) {
                nextSong()
            }
            play++
            onCompletionevent!!.onCompletion(null)
        }
        player!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }


    fun loadOffLine() {
        if (listSong.isNotEmpty()) return
        val cursor: Cursor = App.instance.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
            null, null, MediaStore.Audio.Media.TITLE + " ASC"
        ) ?: return
        cursor.moveToFirst()
        val cursorTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val cursorPath = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val cursorAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val cursorArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val retriever = MediaMetadataRetriever()
        listSong.clear()
        while (!cursor.isAfterLast) {
            val title = cursor.getString(cursorTitle)
            val path = cursor.getString(cursorPath)
            val album = cursor.getString(cursorAlbum)
            val artist = cursor.getString(cursorArtist)
            retriever.setDataSource(path)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val song = Song(title, path, album, artist, duration!!)
            listSong.add(song)
            cursor.moveToNext()
        }
        Log.e(MainFragment.TAG, "List SOng " + MediaManager.getInstance()!!.getListSong().size)
        cursor.close()
    }

    fun pause() {
        if (player!!.isPlaying) {
            player!!.pause()
            state = STATE_PAUSED
            liveData.postValue(state)
        }
    }

    open fun play() {
        when (state) {
            STATE_IDLE -> {
                player!!.reset()
                try {
                    player!!.setDataSource(listSong[currentSong].path)
                    player!!.prepare()
                    player!!.start()
                    state = STATE_PLAYING
                    liveData.postValue(state)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            STATE_PAUSED -> {
                state = STATE_PLAYING
                liveData.postValue(state)
                player!!.start()
            }

            else -> {
                state = STATE_PAUSED
                liveData.postValue(state)
                player!!.pause()
            }
        }
    }


    open fun nextSong() {
        if (shuffle.value == null) return
        if (shuffle.value == 2) {
            currentSong = Random().nextInt(listSong.size)
        } else if (shuffle.value == 0) {
            currentSong++
            if (currentSong >= listSong.size) {
                currentSong = 0
            }
        }
        state = STATE_IDLE
        liveData.postValue(state)
        play()
    }

    fun backSong() {
        if (shuffle.value == null) return
        if (shuffle.value == 2) {
            currentSong = Random().nextInt(listSong.size)
        } else if (shuffle.value == 0) {
            currentSong--
            if (currentSong <= 0) {
                currentSong = listSong.size - 1
            }
        }
        state = STATE_IDLE
        liveData.postValue(state)
        play()
    }

    fun getCurrentSong(): Int {
        return currentSong
    }

    fun setCurrent(current: String) {
        currentSong = current.toInt()
    }

    fun playSong(song: Song?) {
        currentSong = listSong.indexOf(song)
        state = STATE_IDLE
        liveData.postValue(state)
        play()
    }

    fun getSong(): Song {
        return listSong[currentSong]
    }

    fun getCurrentTimeText(): String? {
        try {
            @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("mm:ss")
            return dateFormat.format(Date(player!!.currentPosition.toLong()))
        } catch (ignored: Exception) {
        }
        return "--"
    }

    fun getTotalTimeText(): String? {
        try {
            @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("mm:ss")
            return dateFormat.format(Date(player!!.duration.toLong()))
        } catch (ignored: Exception) {
        }
        return "--"
    }

    fun getTotalTime(): Int {
        return player!!.duration
    }

    fun getCurrentTime(): Int {
        return player!!.currentPosition
    }

    fun seekTo(progress: Int) {
        try {
            player!!.seekTo(progress)
        } catch (ignored: Exception) {
        }
    }

    fun setCompleteCallBack(event: OnCompletionListener) {
        onCompletionevent = event
    }

    fun getPlayer(): Int {
        return player!!.audioSessionId
    }

    fun setShuffle() {
        if (shuffle.value == null) return
        shuffle.postValue(shuffle.value!! + 1)
        if (shuffle.value!! >= 2) {
            shuffle.postValue(0)
        }
    }
}