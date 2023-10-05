package com.clk.musicplayerkotlin

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

interface OnSeekbarChange  : OnSeekBarChangeListener{

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
}