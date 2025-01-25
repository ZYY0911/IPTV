package com.yingyuzhang.iptv.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.yingyuzhang.iptv.adapter.OnClickItemInterface

class DialogUtil {
    fun showExitDialog(
        context: Context,
        onClickItemInterface: OnClickItemInterface<String>,
        title: String? = "退出",
        msg: String? = "确定要退出应用吗?"
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton("确定") { dialog, which ->
            onClickItemInterface.onClickItem("确定")
        }
        builder.setNegativeButton("取消") { dialog, which ->
            onClickItemInterface.onClickItem("")
            dialog.dismiss()
        }
        builder.show()
    }
}