package com.yingyuzhang.iptv.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import com.bumptech.glide.Glide
import com.yingyuzhang.iptv.App
import com.yingyuzhang.iptv.R
import com.yingyuzhang.iptv.adapter.OnClickItemInterface
import com.yingyuzhang.iptv.base.BaseActivity
import com.yingyuzhang.iptv.bean.TvList
import com.yingyuzhang.iptv.databinding.ActivityMainBinding
import com.yingyuzhang.iptv.pop.SettingPopWindows
import com.yingyuzhang.iptv.pop.TvListPopWindows
import com.yingyuzhang.iptv.utils.FileUtils
import com.yingyuzhang.iptv.utils.SharedPreferencesUtil
import java.util.Locale


class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var exoPlayer: ExoPlayer
    private lateinit var bandwidthMeter: DefaultBandwidthMeter

    private var startY: Float = 0f
    private var endY: Float = 0f
    private val SWIPE_THRESHOLD = 200 // 自定义的滑动阈值
    private val LONG_PRESS_DURATION = 1000L // 长按阈值，单位为毫秒
    private var longPressRunnable: Runnable? = null

    private var indexTv = 0

    private var errorTimes = 0;

    private var handler = Handler(Looper.getMainLooper())
    private var showRunnable: Runnable? = null
    private var changeRunnable: Runnable? = null
    private val LONG_CHANGE_DURATION = 1500L // 长按换台，单位为毫秒
    var tvListPop: TvListPopWindows? = null
    var settingPop: SettingPopWindows? = null

    override fun setView(): ConstraintLayout {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    private val updateNetworkSpeedRunnable = @UnstableApi object : Runnable {
        override fun run() {
            val speedInBps = bandwidthMeter.bitrateEstimate.toDouble() / 10
            val speedInKbps = speedInBps / 1000 // 计算 KB/s
            val speedInMbps = speedInKbps / 1000 // 计算 MB/s
            val speedText = when {
                speedInMbps > 0.9 -> String.format(
                    Locale.SIMPLIFIED_CHINESE,
                    "%.1f\nMB/s",
                    speedInMbps
                )

                speedInKbps > 0.9 -> String.format(
                    Locale.SIMPLIFIED_CHINESE,
                    "%.1f\nKB/s",
                    speedInKbps
                )

                else -> String.format(Locale.SIMPLIFIED_CHINESE, "%.2d\nbps", speedInBps)
            }
            binding.tvNetSpeed.text = speedText
            handler.postDelayed(this, 1000)
        }
    }

    @UnstableApi
    override fun initData() {
        super.initData()
        // 初始化带宽监控
        bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        exoPlayer = ExoPlayer.Builder(this)
//            .setMediaSourceFactory(DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .setBandwidthMeter(bandwidthMeter)
            .build()
        for (tvItem in App.getTvList()) {
            val mediaItem = MediaItem.Builder()
                .setUri(tvItem.tvUrl)
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
                )
                .build()
            exoPlayer.addMediaItem(mediaItem)
        }
        indexTv = SharedPreferencesUtil.getInt(SharedPreferencesUtil.TV_INDEX_NUM)
        if (indexTv >= App.getTvList().size) {
            indexTv = 0
        }
        binding.playVideoView.resizeMode =
            if (SharedPreferencesUtil.getString(SharedPreferencesUtil.SHOW_RATIO) == "原始比例")
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            else AspectRatioFrameLayout.RESIZE_MODE_FILL

        //以确保音频配置为立体声（双声道）：
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        exoPlayer.setAudioAttributes(audioAttributes, true)
    }

    override fun initView() {
        super.initView()
        binding.playVideoView.player = exoPlayer
        binding.playVideoView.useController = false
        binding.progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )
    }

    override fun initAdapter() {
        super.initAdapter()
        showRunnable = Runnable {
            run {
                binding.clTvMsg.visibility = View.INVISIBLE
            }
        }
        longPressRunnable = Runnable {
            // 触发长按事件
            binding.viewRight.performLongClick()
        }
        changeRunnable = Runnable {
            var inputNum = tempNum.toInt()
            if (inputNum > App.getTvList().size) {
                inputNum = App.getTvList().size
            }
            indexTv = if (0 == inputNum) 0 else inputNum - 1
            setTvInfo()
            tempNum = ""
            binding.tvInput.visibility = View.INVISIBLE
        }
        setTvInfo()
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
//        exoPlayer.play()
        showLoadAnim()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.i("play", "onPlaybackStateChanged: ${playbackState}")
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        errorTimes++
                        if (errorTimes == 3) {
                            changeTv(1)
                        }
                        exoPlayer.prepare()
                    }

                    Player.STATE_BUFFERING -> {
                        // 显示加载动画
                        showLoadAnim()
                    }

                    Player.STATE_READY -> {
                        disShowLoadAnim()
                    }
                }
            }
        })
        binding.viewRight.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.y
//                    handler.postDelayed(longPressRunnable!!, LONG_PRESS_DURATION)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    endY = event.y
                    val deltaY = endY - startY

//                    handler.removeCallbacks(longPressRunnable!!)

                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0) {
                            // 下滑
                            changeTv(0)
                        } else {
                            // 上滑
                            changeTv(1)
                        }
                    } else {
                        // 点击
                        binding.viewRight.performClick()
                    }
                    true
                }

//                MotionEvent.ACTION_CANCEL -> {
//                    // 如果事件被取消，取消长按处理
//                    handler.removeCallbacks(longPressRunnable!!)
//                    true
//                }

                else -> false
            }
        }

        binding.viewLeft.setOnClickListener { v ->
            showTvListPop()
        }
        binding.viewCenter.setOnClickListener { v ->
            showMenuBar()
            handler.postDelayed(showRunnable!!, 2000)
        }
        binding.viewRight.setOnClickListener { v ->
            showSettingPop()
        }
    }

    private fun disShowLoadAnim() {
        handler.removeCallbacks(updateNetworkSpeedRunnable)
        handler.postDelayed(showRunnable!!, 2000)
        binding.progressBar.visibility = View.GONE
        binding.tvNetSpeed.visibility = View.GONE
        errorTimes = 0
    }

    private fun showLoadAnim() {
        binding.tvNetSpeed.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        handler.post(updateNetworkSpeedRunnable)
    }

    private fun showTvListPop() {
        if (null != settingPop && settingPop!!.isShowing()) {
            settingPop!!.dismiss()
            return
        }
        if (null == tvListPop) {
            tvListPop = TvListPopWindows(this, binding.root)
                .setContentView()
                .showAtLocation()
            tvListPop!!.onClickItemInterface = object : OnClickItemInterface<TvList> {
                override fun onClickItem(msg: TvList) {
                    indexTv = msg.id
                    tvListPop!!.dismiss()
                    setTvInfo()
                }
            }
        } else if (tvListPop!!.isShowing()) {
            tvListPop!!.dismiss()
        } else {
            tvListPop!!.showAtLocation()
        }
    }

    private fun showSettingPop() {
        if (null != tvListPop && tvListPop!!.isShowing()) {
            tvListPop!!.dismiss()
            return
        }
        if (null == settingPop) {
            settingPop = SettingPopWindows(this, binding.root)
                .setContentView()
                .showAtLocation()
            settingPop!!.onClickItemInterface = @UnstableApi object : OnClickItemInterface<String> {
                override fun onClickItem(msg: String) {
                    when (msg) {
                        "原始比例" -> {
                            binding.playVideoView.resizeMode =
                                AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }

                        "适应屏幕" -> {
                            binding.playVideoView.resizeMode =
                                AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                        //TODO 16:9 and 4:3
//                        "16:9" -> {
//                            binding.playVideoView.resizeMode =
//                                AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
//                        }
//
//                        "4:3" -> {
//                            binding.playVideoView.resizeMode =
//                                AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
//                        }

                        "IPv6" -> {
                            SharedPreferencesUtil.putBooleanAfterFinish(
                                SharedPreferencesUtil.IS_IPV6,
                                true
                            )
                            settingPop!!.dismiss()
                            App.restartApp(this@MainActivity, LaunchActivity::class.java)
                            finish()
                        }

                        "IPv4" -> {
                            SharedPreferencesUtil.putBooleanAfterFinish(
                                SharedPreferencesUtil.IS_IPV6,
                                false
                            )
                            settingPop!!.dismiss()
                            App.restartApp(this@MainActivity, LaunchActivity::class.java)
                            finish()
                        }
                    }
                }
            }
            settingPop!!.onChangeRadio = object : SettingPopWindows.OnChangeRadio {
                override fun changeRadio(type: Int) {
                    startActivity(Intent(this@MainActivity, RadioActivity::class.java))
                    finish()
                }
            }
        } else if (settingPop!!.isShowing()) {
            settingPop!!.dismiss()
        } else {
            settingPop!!.showAtLocation()
        }
    }

    /**
     * tag = 0 下滑->减 1 上滑->加
     */
    private fun changeTv(tag: Int) {
        if (tag == 0) {
            indexTv--
            if (indexTv < 0)
                indexTv = App.getTvList().size - 1
        } else if (tag == 1) {
            indexTv++
            if (indexTv == App.getTvList().size)
                indexTv = 0
        }
        setTvInfo()
    }

    private fun setTvInfo() {
        errorTimes = 0
        exoPlayer.seekTo(indexTv, 0)
        val info = App.getTvList()[indexTv]
        binding.tvGroup.text = info.groupTitle
        binding.tvName.text = info.tvName
        binding.tvNum.text = "${indexTv + 1}"
        binding.tvDes.text = "当前播放 : ${FileUtils().parseXmlFromCache(this, info.tvgName)}"
        Glide.with(this)
            .load(info.tvgLogo)
            .placeholder(R.mipmap.tupianjiazaishibai)
            .into(binding.tvPng)
        showMenuBar()
    }

    fun isIPv6Url(url: String): Boolean {
        val regex =
            Regex("^(https?):\\/\\/\\[(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}\\](?::\\d+)?(\\/.*)?$")
        return regex.matches(url)
    }

    private fun showMenuBar() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                handler.hasCallbacks(showRunnable!!)
            } else {
                true
            }
        ) {
            handler.removeCallbacks(showRunnable!!)
        }
        binding.clTvMsg.visibility = View.VISIBLE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                // 处理向上键
                return if (isShowPop()) {
                    super.onKeyDown(keyCode, event)
                } else {
                    changeTv(1)
                    true
                }
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                // 处理向下键
                return if (isShowPop()) {
                    super.onKeyDown(keyCode, event)
                } else {
                    changeTv(0)
                    true
                }
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                // 处理向左键
                return if (isShowPop()) {
                    super.onKeyDown(keyCode, event)
                } else {
                    showTvListPop()
                    true
                }
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // 处理向右键
                return if (isShowPop()) {
                    super.onKeyDown(keyCode, event)
                } else {
                    showSettingPop()
                    true
                }
            }

//            KeyEvent.KEYCODE_ENTER -> {
//                // 处理回车键
//                return true
//            }

            KeyEvent.KEYCODE_DPAD_CENTER -> {
                //处理中心键
                showMenuBar()
                handler.postDelayed(showRunnable!!, 2000)
                return true
            }

            KeyEvent.KEYCODE_SETTINGS -> {
                showSettingPop()
                return true
            }

            KeyEvent.KEYCODE_BACK -> {
//                showExitDialog()
                onBackPressed()
                return true
            }

            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9 -> {
                // 处理数字键
                handleNumberKeyPress(keyCode)
                return true
            }

            else -> return super.onKeyDown(keyCode, event)
        }
    }

    var tempNum = ""

    private fun handleNumberKeyPress(keyCode: Int) {
        binding.tvInput.visibility = View.VISIBLE
        val channelNumber = when (keyCode) {
            KeyEvent.KEYCODE_0 -> 0
            KeyEvent.KEYCODE_1 -> 1
            KeyEvent.KEYCODE_2 -> 2
            KeyEvent.KEYCODE_3 -> 3
            KeyEvent.KEYCODE_4 -> 4
            KeyEvent.KEYCODE_5 -> 5
            KeyEvent.KEYCODE_6 -> 6
            KeyEvent.KEYCODE_7 -> 7
            KeyEvent.KEYCODE_8 -> 8
            KeyEvent.KEYCODE_9 -> 9
            else -> null
        }
        tempNum += channelNumber
        binding.tvInput.text = tempNum
        handler.removeCallbacks(changeRunnable!!)
        if (tempNum.length == 3) {
            handler.post(changeRunnable!!)
        } else {
            handler.postDelayed(changeRunnable!!, LONG_CHANGE_DURATION)
        }
    }

    private var startTime = 0L
    private var toast: Toast? = null
    override fun onBackPressed() {
        if (null != tvListPop && tvListPop!!.isShowing()) {
            tvListPop!!.dismiss()
        } else if (null != settingPop && settingPop!!.isShowing()) {
            settingPop!!.dismiss()
        } else {
            var currentTime = System.currentTimeMillis()
            if (currentTime - startTime >= 2000) {
                startTime = currentTime
                if (null == toast)
                    toast = Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT)
                toast?.show()
            } else {
                toast?.cancel()
                startTime = 0
                super.onBackPressed()
            }
//            super.onBackPressed()
//            showExitDialog()
//            DialogUtil().showExitDialog(this, object : OnClickItemInterface<String> {
//                override fun onClickItem(msg: String) {
//                    if (msg.isNotEmpty())
//                        onBackPressedDispatcher.onBackPressed()
//                }
//            })
        }
    }

    fun isShowPop(): Boolean {
//        if (null!=tvListPop &&tvListPop!!.isShowing()){
//            return true
//        }
//        if (null != settingPop && settingPop!!.isShowing()) {
//            return true
//        }
//        return false
        return (tvListPop?.isShowing() == true) || (settingPop?.isShowing() == true)
    }

    override fun onResume() {
        super.onResume()
        if (isNotFirstShow) {
            if (!exoPlayer.isPlaying) {
                exoPlayer.seekForward()
                exoPlayer.play()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferencesUtil.putInt(SharedPreferencesUtil.TV_INDEX_NUM, indexTv)
        exoPlayer.release()
    }
}