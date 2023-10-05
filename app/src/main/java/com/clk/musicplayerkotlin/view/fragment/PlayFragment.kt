package com.clk.musicplayerkotlin.view.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.clk.musicplayerkotlin.MediaManager
import com.clk.musicplayerkotlin.OnSeekbarChange
import com.clk.musicplayerkotlin.R
import com.clk.musicplayerkotlin.databinding.FragmentPlayBinding
import com.clk.musicplayerkotlin.model.Song
import com.clk.musicplayerkotlin.view.fragment.MainFragment.Companion.LEVER_IDLE
import com.clk.musicplayerkotlin.view.fragment.MainFragment.Companion.LEVER_PLAY
import com.clk.musicplayerkotlin.view.viewmodel.CommonVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PlayFragment : BaseFragment<FragmentPlayBinding, CommonVM>()  , CoroutineScope{
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()
    companion object {
        val TAG: String = PlayFragment::class.java.name
    }
    override fun initViews() {
        binding.ivPlay.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.ivNext.setOnClickListener(this)
        binding.ivOpen.setOnClickListener(this)
        binding.ivShuffle.setOnClickListener(this)
        binding.menu.ivMenu.setOnClickListener(this)
        binding.menu.ivMenu.setImageResource(R.drawable.ic_back_frg)
        MediaManager.getInstance()!!.getShuffle().observe(this) {
            when (it) {
                0 -> {  binding.ivShuffle.setImageLevel(0)    }
                1 -> {  binding.ivShuffle.setImageLevel(1)  }
                2 -> {   binding.ivShuffle.setImageLevel(2)  }
            }
        }
        launch { async {
                binding.blast.setColor(ContextCompat.getColor(mContext, R.color.custom))
                binding.blast.setDensity(70F)
                binding.blast.setPlayer(MediaManager.getInstance()!!.getPlayer())
                updateBeekbar(binding.seekbar, binding.tvDuration) }
        }
        upDateUi(binding.ivPlay, binding.tvName, binding.tvAlbum)
        MediaManager.getInstance()!!.getLiveData().observe(this) {
        upDateUi(  binding.ivPlay,   binding.tvName, binding.tvAlbum  )  }
        binding.seekbar.setOnSeekBarChangeListener(object  :   OnSeekbarChange {  override fun onStopTrackingTouch(seekBar: SeekBar?) =  MediaManager.getInstance()!!.seekTo(seekBar!!.progress)  })
    }
    private fun updateBeekbar(seekbar: SeekBar, tvDuration: TextView) {
        val appRunning = true
        while (appRunning) {
            try {
                Thread.sleep(500)
                launch(Dispatchers.Main) {
                    val currentTimeText = MediaManager.getInstance()!!.getCurrentTimeText()
                    val totalTimeText = MediaManager.getInstance()!!.getTotalTimeText()
                    val currentTime = MediaManager.getInstance()!!.getCurrentTime()
                    val totalTime = MediaManager.getInstance()!!.getTotalTime()
                    seekbar.max = totalTime
                    seekbar.progress = currentTime
                    tvDuration.text = String.format("%s/%s", currentTimeText, totalTimeText)
                }
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
    }
    private fun upDateUi(ivPlay: ImageView, tvName: TextView, tvAlbum: TextView) {
        if (MediaManager.getInstance()!!.getState() == MediaManager.STATE_PLAYING) {
            ivPlay.setImageLevel(LEVER_PLAY)
        } else {
            ivPlay.setImageLevel(LEVER_IDLE)
        }
        val song: Song = MediaManager.getInstance()!!.getSong()
        tvName.text = song.title
        tvAlbum.text = song.album
    }

    override fun clickView(v: View) {
        when (v.id) {  R.id.iv_play -> {  MediaManager.getInstance()!!.play()  }
            R.id.iv_next -> {  MediaManager.getInstance()!!.nextSong()  }
            R.id.iv_back -> {   MediaManager.getInstance()!!.backSong()  }
            R.id.iv_menu, R.id.iv_open -> {  callBack.backToPrevious()   }
            R.id.iv_shuffle -> {   MediaManager.getInstance()!!.setShuffle()    }
        }
    }
    override fun onDestroy() {
        binding.blast.release()
        super.onDestroy()
    }
    override fun getClassViewModel(): Class<CommonVM> = CommonVM::class.java

    override fun initViewBinding(  inflater: LayoutInflater, container: ViewGroup? ): FragmentPlayBinding = FragmentPlayBinding.inflate(inflater, container, false)
}