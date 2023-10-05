package com.clk.musicplayerkotlin.view.activity

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.clk.musicplayerkotlin.CommonUtils
import com.clk.musicplayerkotlin.MediaManager
import com.clk.musicplayerkotlin.databinding.ActivityMainBinding
import com.clk.musicplayerkotlin.view.fragment.MainFragment
import com.clk.musicplayerkotlin.view.viewmodel.CommonVM

class MainActivity : BaseActivity<ActivityMainBinding, CommonVM>() {
    companion object {
        val TAG : String = MainActivity::class.java.name
        private val REQUEST_AUDIO_PERMISSION = 1002
        private val REQUEST_STORAGE_PERMISSION = 1003
    }

    override fun initViews() {
        checkPermission()
    }

    private fun checkPermission() {
        val audioPermission = Manifest.permission.RECORD_AUDIO
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val hasStoragePermission = ActivityCompat.checkSelfPermission(
            this,
            storagePermission
        ) == PackageManager.PERMISSION_GRANTED
        val hasAudioPermission = ActivityCompat.checkSelfPermission(
            this,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasAudioPermission || !hasStoragePermission) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                ), 101
            )
        } else {
            showFragment(MainFragment.TAG, null, false)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            showFragment(MainFragment.TAG, null, false)
        }
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java
    override fun initViewBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)
}