package com.clk.musicplayerkotlin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.clk.musicplayerkotlin.App
import com.clk.musicplayerkotlin.MediaManager
import com.clk.musicplayerkotlin.R
import com.clk.musicplayerkotlin.model.Song
import com.clk.musicplayerkotlin.view.activity.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MediaService : Service(), CoroutineScope {
    private var views: RemoteViews? = null
    private var song: Song? = null
    private var appRunning = false
    private var notify: Notification? = null
    private var viewCollapse: RemoteViews? = null
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + Job()

    companion object {
        var TAG : String = MediaService::class.java.name
        private const val CHANNEL_ID = "music"
        private const val PLAY_EVENT = "PLAY_EVENT"
        private const val NEXT_EVENT = "NEXT_EVENT"
        private const val BACK_EVENT = "BACK_EVENT"
        private const val CLOSE_EVENT = "CLOSE_EVENT"
        private const val KEY_EVENT = "KEY_EVENT"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        views = RemoteViews(packageName, R.layout.item_notify_media)
        viewCollapse = RemoteViews(packageName, R.layout.item_notify_collapsed)
        song = MediaManager.getInstance()!!.getSong()
        viewCollapse!!.setTextViewText(R.id.tv_name, song!!.title)
        viewCollapse!!.setTextViewText(R.id.tv_album, song!!.album)
        views!!.setTextViewText(R.id.tv_name, song!!.title)
        views!!.setTextViewText(R.id.tv_album, song!!.album)
        views!!.setOnClickPendingIntent(R.id.iv_close, getPendingIntent(1, CLOSE_EVENT))
        views!!.setOnClickPendingIntent(R.id.iv_play, getPendingIntent(2, PLAY_EVENT))
        views!!.setOnClickPendingIntent(R.id.iv_next, getPendingIntent(3, NEXT_EVENT))
        views!!.setOnClickPendingIntent(R.id.iv_back, getPendingIntent(5, BACK_EVENT))
        val intent = Intent(App.instance, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent =
            PendingIntent.getActivity(App.instance, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        appRunning = true
        launch {
            async { updateBeekbar() }
        }
        builder.setSmallIcon(R.drawable.ic_mp3)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setChannelId(CHANNEL_ID)
            .setContentTitle(song!!.title)
            .setContentText(song!!.album)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setSound(null)
            .setCustomContentView(viewCollapse)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notify = builder.build()
        startForeground(1001, notify)
    }

    private fun updateBeekbar() {
        while (appRunning) {
            try {
                Thread.sleep(1000)
                upDateUi()
                Log.e(TAG , "CHANNNNNN")
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
    }

    private fun upDateUi() {
        launch(Dispatchers.Main) {
            song = MediaManager.getInstance()!!.getSong()
            val currentTimeText = MediaManager.getInstance()!!.getCurrentTimeText()
            val totalTimeText = MediaManager.getInstance()!!.getTotalTimeText()
            val currentTime = MediaManager.getInstance()!!.getCurrentTime()
            val totalTime = MediaManager.getInstance()!!.getTotalTime()
            views!!.setTextViewText(R.id.tv_name, song!!.title)
            views!!.setTextViewText(R.id.tv_album, song!!.album)
            viewCollapse!!.setTextViewText(R.id.tv_name, song!!.title)
            viewCollapse!!.setTextViewText(R.id.tv_album, song!!.album)
            views!!.setProgressBar(R.id.progressbar, totalTime, currentTime, false)
            viewCollapse!!.setProgressBar(R.id.progressbar, totalTime, currentTime, false)
            views!!.setTextViewText(
                R.id.tv_Duration,
                String.format("%s/%s", currentTimeText, totalTimeText)
            )
            viewCollapse!!.setTextViewText(
                R.id.tv_Duration,
                String.format("%s/%s", currentTimeText, totalTimeText)
            )
            if (MediaManager.getInstance()!!.getState() == MediaManager.STATE_PLAYING) {
                views!!.setImageViewResource(R.id.iv_play, R.drawable.ic_pause)
            } else {
                views!!.setImageViewResource(R.id.iv_play, R.drawable.ic_play)
            }
            startForeground(1001, notify)
        }
    }

    private fun getPendingIntent(number: Int, key: String): PendingIntent? {
        val intent = Intent(this, MediaService::class.java)
        intent.putExtra(KEY_EVENT, key)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getService(this, number, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        val description = "Enjoy music :))"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel: NotificationChannel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance)
            channel.description = description
            channel.setSound(null, null)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val key = intent.getStringExtra(KEY_EVENT)
            if (key != null) {
                when (key) {
                    NEXT_EVENT -> {
                        MediaManager.getInstance()!!.nextSong()
                        upDateUi()
                    }

                    BACK_EVENT -> {
                        MediaManager.getInstance()!!.backSong()
                        upDateUi()
                    }

                    PLAY_EVENT -> {
                        MediaManager.getInstance()!!.play()
                        upDateUi()
                    }

                    CLOSE_EVENT -> {
                        MediaManager.getInstance()!!.pause()
                        stopService(Intent(this, MediaService::class.java))
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}