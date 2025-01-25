package com.yingyuzhang.iptv.pop

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import com.yingyuzhang.iptv.App
import com.yingyuzhang.iptv.adapter.OnClickItemInterface
import com.yingyuzhang.iptv.adapter.TvListAdapter
import com.yingyuzhang.iptv.bean.TvList
import com.yingyuzhang.iptv.databinding.ShowListPopBinding

class TvListPopWindows(
    private var mContext: Context,
    private var mParent: ViewGroup
) {
    private val FULL_SCREEN_FLAG = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN)

    var popupWindow: PopupWindow = PopupWindow(mContext)
    var tvListAdapter: TvListAdapter? = null
    var onClickItemInterface: OnClickItemInterface<TvList>? = null
    lateinit var binding: ShowListPopBinding

    fun setContentView(): TvListPopWindows {
        binding =
            ShowListPopBinding.inflate(mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        popupWindow.contentView = binding.root
        popupWindow.height = ViewGroup.LayoutParams.MATCH_PARENT
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isOutsideTouchable = false
        // 设置 PopupWindow 可以获取焦点
        popupWindow.isFocusable = true
        popupWindow.contentView.systemUiVisibility = FULL_SCREEN_FLAG;
        initAdapter()
        return this
    }

    fun showAtLocation(): TvListPopWindows {
        showAtLocation(Gravity.LEFT, 0, 0)
        return this
    }

    fun showAtLocation(gravity: Int, x: Int, y: Int): TvListPopWindows {
        popupWindow.showAtLocation(mParent, gravity, x, y)
        return this
    }

    fun dismiss() {
        popupWindow.dismiss()
    }

    fun isShowing(): Boolean {
        return popupWindow.isShowing
    }


    private fun initAdapter() {
        tvListAdapter = TvListAdapter(mContext)
        tvListAdapter?.setData(App.getTvList())
//        binding.rvList.layoutManager = GridLayoutManager(mContext, 5)
        binding.rvList.adapter = tvListAdapter
        tvListAdapter!!.onClickItemInterface = object : OnClickItemInterface<TvList> {
            override fun onClickItem(msg: TvList) {
                if (null != onClickItemInterface) onClickItemInterface!!.onClickItem(msg)
            }
        }
    }
}