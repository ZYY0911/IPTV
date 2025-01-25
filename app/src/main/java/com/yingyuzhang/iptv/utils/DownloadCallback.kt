package com.yingyuzhang.iptv.utils


interface DownloadCallback {
    fun onDownloadSuccess(file:String)
    fun onDownloadFailure(exception: Exception)
}