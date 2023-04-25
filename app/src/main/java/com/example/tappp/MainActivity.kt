package com.example.tappp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.example.tappp.databinding.ActivityMainBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.tappp.library.context.TapppContext
import com.tappp.library.model.PanelSettingModel
import com.tappp.library.view.TapppPanel
import org.json.JSONObject

class MainActivity : AppCompatActivity(),Player.Listener {

    private lateinit var binding: ActivityMainBinding
    private val BroadCastName: String = TapppContext.Sports.TRN

    private var GameID = "cb0403c8-0f3c-4778-8d26-c4a63329678b"
    private var UserID = "cf9bb061-a040-4f43-9165-dac3adfb4258"

    private lateinit var tapppPanel: TapppPanel
    private var currentWindow = 0

    private var playbackPosition: Long = 0
    private var isFullscreen = false

    private var isPlayerPlaying = true

    private lateinit var frameContainer: FrameLayout
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var exoPlayerView: PlayerView


    companion object {
        var HLS_STATIC_URL =
            "https://sandbox-tappp.s3.us-east-2.amazonaws.com/content/videos/full_UTAHvTOR_480.mp4"
        const val STATE_RESUME_WINDOW = "resumeWindow"
        const val STATE_RESUME_POSITION = "resumePosition"
        const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
        const val STATE_PLAYER_PLAYING = "playerOnPlay"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // disable action bar
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(binding.root)

        exoPlayerView = findViewById(R.id.exPlayer)
        frameContainer = findViewById(R.id.fragment_container)

        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
        exoPlayerView.player = exoPlayer
        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(HLS_STATIC_URL)
            .setMimeType(if (HLS_STATIC_URL.contains(".mp4")) MimeTypes.APPLICATION_MP4 else MimeTypes.APPLICATION_M3U8)
            .build()
        exoPlayer.addMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        exoPlayerView.player!!.addListener(this)

        initPanel()

    }

    private fun initPanel() {
        val panelSetting = PanelSettingModel()
        panelSetting.supportManager = supportFragmentManager
        panelSetting.panelView = frameContainer

        val panelData = JSONObject()
        panelData.put(TapppContext.Sports.GAME_ID, GameID)
        panelData.put(TapppContext.Sports.BROADCASTER_NAME, BroadCastName)
        panelData.put(TapppContext.User.USER_ID, UserID)
        val tapppContext = JSONObject()
        tapppContext.put(TapppContext.Sports.Context, panelData)

        tapppPanel = TapppPanel(this)
        tapppPanel.initPanel(tapppContext, panelSetting)
        tapppPanel.startPanel()
    }


    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        exoPlayerView.player!!.prepare()
        Toast.makeText(
            this,
            "Something Went wrong",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun releasePlayer() {
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentMediaItemIndex
        exoPlayer.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentMediaItemIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            exoPlayerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Util.SDK_INT > 23) {
            exoPlayerView.onPause()
            releasePlayer()
        }
    }
}