package com.yingyuzhang.iptv.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.yingyuzhang.iptv.R

abstract class BaseActivity : AppCompatActivity() {
    var isNotFirstShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(setView())
        if (isStatusBarEnabled()) {
            getStatusBarConfig().init()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initData()
        initView()
        initAdapter()
        isNotFirstShow = true
    }

    abstract fun setView(): ConstraintLayout

    protected open fun initView() {}
    protected open fun initData() {}

    protected open fun initAdapter() {}

    protected open fun isStatusBarEnabled(): Boolean {
        return true
    }

    /**
     * 状态栏沉浸
     */
    private var mImmersionBar: ImmersionBar? = null
    open fun getStatusBarConfig(): ImmersionBar {
        if (mImmersionBar == null) {
            mImmersionBar = createStatusBarConfig()
        }
        return mImmersionBar!!
    }

    protected open fun createStatusBarConfig(): ImmersionBar {
        return ImmersionBar.with(this)
            .statusBarColor(R.color.white)// 默认状态栏字体颜色为黑色
            .statusBarDarkFont(
                true,
                0.2f
            )//原理：如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色，如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
            .navigationBarColor(R.color.white) // 指定导航栏背景颜色
            .autoDarkModeEnable(true, 0.2f)// 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
            .hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR)//隐藏导航栏
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
//
//    inline fun <reified T : Activity> Context.startActivity(){
//        val intent = Intent(this, T::class.java)
//        startActivity(intent)
//        finish()
//    }
}