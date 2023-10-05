package com.clk.musicplayerkotlin

interface OnMainCallBack {
    fun showFragment(tag: String, data: Any?, isBacked: Boolean)

    fun backToPrevious()
}