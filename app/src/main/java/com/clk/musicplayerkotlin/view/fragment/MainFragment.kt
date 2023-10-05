package com.clk.musicplayerkotlin.view.fragment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.clk.musicplayerkotlin.CommonUtils
import com.clk.musicplayerkotlin.MediaManager
import com.clk.musicplayerkotlin.OnSeekbarChange
import com.clk.musicplayerkotlin.R
import com.clk.musicplayerkotlin.adapter.SongAdapter
import com.clk.musicplayerkotlin.databinding.FragmentManiBinding
import com.clk.musicplayerkotlin.model.Song
import com.clk.musicplayerkotlin.service.MediaService
import com.clk.musicplayerkotlin.view.viewmodel.CommonVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

open class MainFragment : BaseFragment<FragmentManiBinding, CommonVM>()  , CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()
    private lateinit var adapter: SongAdapter
    private var layoutManager: LinearLayoutManager? = null
    companion object {
        val TAG: String = MainFragment::class.java.name
        const val LEVER_PLAY = 1
        const val LEVER_IDLE = 0

    }
    override fun initViews() {
        binding.controller.ivBack.setOnClickListener(this)
        binding.controller.ivNext.setOnClickListener(this)
        binding.controller.ivPlay.setOnClickListener(this)
        binding.controller.ivOpen.setOnClickListener(this)
        binding.controller.ivShuffle.setOnClickListener(this)
        binding.menu.ivMenu.setImageResource(R.drawable.ic_menu)
        layoutManager = LinearLayoutManager(context)
        binding.rcSong.layoutManager = layoutManager
        MediaManager.getInstance()!!.loadOffLine()
        val current: String? = CommonUtils.getInstance().getPref("KEY_CURRENT")
        if (current != null) {
            MediaManager.getInstance()!!.setCurrent(current)
            initListSong()
            adapter.setCurrent(current) } else { initListSong() }
        MediaManager.getInstance()!!.getLiveData().observe(this) { updateUi() }
        MediaManager.getInstance()!!.getShuffle().observe(this) { setUpImageShuffle(it) }
        MediaManager.getInstance()!!.setCompleteCallBack { upDateUi(binding.controller.ivPlay, binding.controller.tvName, binding.controller.tvAlbum) }
        binding.controller.seekbar.setOnSeekBarChangeListener(object  : OnSeekbarChange{  override fun onStopTrackingTouch(seekBar: SeekBar?) =  MediaManager.getInstance()!!.seekTo(seekBar!!.progress)  })
    }

    private fun setUpImageShuffle(it: Int?) {
        when (it) {0 -> { binding.controller.ivShuffle.setImageLevel(0) }
            1 -> { binding.controller.ivShuffle.setImageLevel(1) }
            2 -> { binding.controller.ivShuffle.setImageLevel(2) } }
    }

    override fun clickView(v: View) {
        when (v.id) {R.id.iv_play -> { MediaManager.getInstance()!!.play()
            }R.id.iv_next -> { MediaManager.getInstance()!!.nextSong()
                adapter.upDateUi(MediaManager.getInstance()!!.getCurrentSong())
            }R.id.iv_back -> { MediaManager.getInstance()!!.backSong()
                adapter.upDateUi(MediaManager.getInstance()!!.getCurrentSong())
            }R.id.tb_song -> { callBack.showFragment(PlayFragment.TAG, null, true)
                adapter.upDateUi(MediaManager.getInstance()!!.getCurrentSong())
                MediaManager.getInstance()!!.playSong(v.tag as Song) }
            R.id.iv_open -> { callBack.showFragment(PlayFragment.TAG, null, true) }
            R.id.iv_shuffle -> { MediaManager.getInstance()!!.setShuffle() }
        }
    }
    private fun updateUi() {
        layoutManager!!.smoothScrollToPosition(binding.rcSong,  null,  MediaManager.getInstance()!!.getCurrentSong())
        upDateUi(binding.controller.ivPlay, binding.controller.tvName, binding.controller.tvAlbum)
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

    private fun initListSong() {
        adapter = SongAdapter(MediaManager.getInstance()!!.getListSong() , context) { this.clickView(it) }
        binding.rcSong.adapter = adapter
        requireContext().startService(Intent(context, MediaService::class.java))
       launch{ async { updateBeekbar(binding.controller.seekbar, binding.controller.tvDuration) }  }
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
    override fun onPause() {
        CommonUtils.getInstance().savePref(  "KEY_CURRENT", java.lang.String.valueOf(   MediaManager.getInstance()!!.getCurrentSong()))
        super.onPause()
    }
    override fun getClassViewModel(): Class<CommonVM> = CommonVM::class.java
    override fun initViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentManiBinding = FragmentManiBinding.inflate(inflater, container, false)
}