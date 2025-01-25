package com.yingyuzhang.iptv.pop

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import com.yingyuzhang.iptv.BuildConfig
import com.yingyuzhang.iptv.R
import com.yingyuzhang.iptv.activity.MainActivity
import com.yingyuzhang.iptv.adapter.OnClickItemInterface
import com.yingyuzhang.iptv.databinding.SettingPopBinding
import com.yingyuzhang.iptv.utils.DialogUtil
import com.yingyuzhang.iptv.utils.FileUtils
import com.yingyuzhang.iptv.utils.SharedPreferencesUtil
import kotlin.system.exitProcess


class SettingPopWindows(
    private var mContext: Context,
    private var mParent: ViewGroup
) {

    interface OnChangeRadio {
        fun changeRadio(type: Int)
    }


    private val FULL_SCREEN_FLAG = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN)

    private var popupWindow: PopupWindow = PopupWindow(mContext)
    lateinit var binding: SettingPopBinding
    var netRb = true

    var checkId = 0
    var onClickItemInterface: OnClickItemInterface<String>? = null

    var onChangeRadio: OnChangeRadio? = null


    fun setContentView(): SettingPopWindows {
        binding =
            SettingPopBinding.inflate(mContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        popupWindow.contentView = binding.root
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = false
        // 设置 PopupWindow 可以获取焦点
        popupWindow.isFocusable = true
        popupWindow.contentView.systemUiVisibility = FULL_SCREEN_FLAG;

        initView()
        initAdapter()
        return this
    }

    private fun initAdapter() {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (!netRb) return@setOnCheckedChangeListener
            netRb = false
            val tempRb = binding.root.findViewById<RadioButton>(checkedId)
            val tempText = tempRb.text.toString()
            DialogUtil().showExitDialog(mContext, object : OnClickItemInterface<String> {
                override fun onClickItem(msg: String) {
                    if (msg.isEmpty()) {
                        group.check(if (checkedId == R.id.rb_v6) R.id.rb_v4 else R.id.rb_v6)
                        netRb = true
                        return
                    }
                    if (null != onClickItemInterface) onClickItemInterface!!.onClickItem(tempText)
                }
            }, "提示", "确定要修改成${tempText},确定后App自动退出")
        }
        binding.radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            val tempRb = binding.root.findViewById<RadioButton>(checkedId).text.toString()
            SharedPreferencesUtil.putString(SharedPreferencesUtil.SHOW_RATIO, tempRb)
            if (null != onClickItemInterface) onClickItemInterface!!.onClickItem(tempRb)
        }
        binding.btReApp.setOnClickListener { v ->
            DialogUtil().showExitDialog(mContext, object : OnClickItemInterface<String> {
                override fun onClickItem(msg: String) {
                    if (msg.isNotEmpty()) {
                        FileUtils().clearCache(mContext)
                        (mContext as MainActivity).finishAffinity()
                        SharedPreferencesUtil.clear()
                        exitProcess(0)
                    }
                }
            }, "提示", "重置会初始化设置，是否确定重置应用？")
        }
        binding.radioGroup3.setOnCheckedChangeListener { group, checkedId ->
            val tempRb = binding.root.findViewById<RadioButton>(checkedId)
            when (tempRb.text.toString()) {
                "开" -> {
                    SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.IS_AUTO_UPDATE, true)
                }

                "关" -> {
                    if (!netRb) return@setOnCheckedChangeListener
                    netRb = false
                    DialogUtil().showExitDialog(mContext, object : OnClickItemInterface<String> {
                        override fun onClickItem(msg: String) {
                            if (msg.isEmpty()) {
                                group.check(R.id.rb_open)
                            } else {
                                SharedPreferencesUtil.putBoolean(
                                    SharedPreferencesUtil.IS_AUTO_UPDATE,
                                    false
                                )
                            }
                            netRb = true
                        }
                    }, "提示", "关闭后会使节目预告失效，是否关闭?")
                }
            }
        }
        binding.btReRadio.setOnClickListener { v ->
            if (null != onChangeRadio) {
                onChangeRadio?.changeRadio(0)
            }
        }
    }

    private fun initView() {
        binding.tvVersion.text = "Ver:${BuildConfig.VERSION_NAME}"
        binding.radioGroup.check(if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_IPV6)) R.id.rb_v6 else R.id.rb_v4)
        binding.radioGroup2.check(if (SharedPreferencesUtil.getString(SharedPreferencesUtil.SHOW_RATIO) == "原始比例") R.id.rb_init else R.id.rb_fill)
        binding.radioGroup3.check(if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_AUTO_UPDATE)) R.id.rb_open else R.id.rb_close)
    }

    fun showAtLocation(): SettingPopWindows {
        showAtLocation(Gravity.LEFT, 0, 0)
        return this
    }

    fun showAtLocation(gravity: Int, x: Int, y: Int): SettingPopWindows {
        popupWindow.showAtLocation(mParent, gravity, x, y)
        return this
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    fun isShowing(): Boolean {
        return popupWindow.isShowing
    }


}