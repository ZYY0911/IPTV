package com.yingyuzhang.iptv.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.yingyuzhang.iptv.App
import com.yingyuzhang.iptv.R
import com.yingyuzhang.iptv.base.BaseActivity
import com.yingyuzhang.iptv.databinding.ActivityLaunchBinding
import com.yingyuzhang.iptv.utils.DownloadCallback
import com.yingyuzhang.iptv.utils.FileUtils
import com.yingyuzhang.iptv.utils.NetUtils
import com.yingyuzhang.iptv.utils.SharedPreferencesUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LaunchActivity : BaseActivity() {
    lateinit var binding: ActivityLaunchBinding
    override fun setView(): ConstraintLayout {
        binding = ActivityLaunchBinding.inflate(layoutInflater)
        return binding.root
    }

    private var isUpdateToday = false

    override fun initView() {
        super.initView()

        binding.progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(this, R.color.white),
            PorterDuff.Mode.SRC_IN
        )

    }

    override fun initData() {
        super.initData()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val saveDate = Date(
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_IPV6))
                SharedPreferencesUtil.getLong(SharedPreferencesUtil.TV_UPDATE_V6)
            else SharedPreferencesUtil.getLong(SharedPreferencesUtil.TV_UPDATE_V4)
        )
        isUpdateToday = dateFormat.format(saveDate) != dateFormat.format(Date())
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_AUTO_UPDATE)) {
            isUpdateToday = false
        }
        if (App.getTvList().size == 0) {
            if (!FileUtils().existList(this, App.getTvFileName()) || isUpdateToday) {
                FileUtils().downloadFile(
                    App.getTvListUrl(),
                    App.getTvFileName(),
                    this,
                    object : DownloadCallback {
                        override fun onDownloadSuccess(file: String) {
                            if (file.isEmpty()) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@LaunchActivity,
                                        "视频源获取失败，请重启App或到设置中手动更新",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_IPV6))
                                    SharedPreferencesUtil.putLong(
                                        SharedPreferencesUtil.TV_UPDATE_V6,
                                        System.currentTimeMillis()
                                    )
                                else SharedPreferencesUtil.putLong(
                                    SharedPreferencesUtil.TV_UPDATE_V4,
                                    System.currentTimeMillis()
                                )
                            }
                            getShowTime()
                        }

                        override fun onDownloadFailure(exception: Exception) {
                            onDownloadSuccess("")
                        }
                    })
            } else {
                getShowTime()
            }
        } else {
            startActivity<MainActivity>(false)
        }
    }

    fun getShowTime() {
        FileUtils().parseTvListFromFile(this)
        if (!FileUtils().existList(this, "e.xml") || isUpdateToday) {
            FileUtils().downloadFile(App.getTvShowTime(), "e.xml", this, object : DownloadCallback {
                override fun onDownloadSuccess(file: String) {
                    if (file.isEmpty()) {
                        runOnUiThread {
                            Toast.makeText(
                                this@LaunchActivity,
                                "预告单下载失败，请重启App或到设置中手动更新",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    startActivity<MainActivity>()
                }

                override fun onDownloadFailure(exception: Exception) {
                    onDownloadSuccess("")
                }
            })
        } else {
            startActivity<MainActivity>()
        }
    }

    inline fun <reified T : Activity> Context.startActivity(isCheck: Boolean? = true) {
        if (isCheck == true) {
            NetUtils().isIPv6Supported(this, object : DownloadCallback {
                override fun onDownloadSuccess(file: String) {
                    var showString = if (file.isEmpty()) {
                        "IPv6连接失败，请重试！"
                    } else if (file == "IPv4") {
                        "IPv4"
                    } else {
                        "IPv6连接成功！"
                    }
                    runOnUiThread {
                        Toast.makeText(
                            this@LaunchActivity,
                            "$showString\n$file",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        val intent = Intent(this@LaunchActivity, T::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onDownloadFailure(exception: Exception) {
                    onDownloadSuccess("")
                }
            })
        } else {
            val intent = Intent(this, T::class.java)
            startActivity(intent)
            finish()
        }
    }

}