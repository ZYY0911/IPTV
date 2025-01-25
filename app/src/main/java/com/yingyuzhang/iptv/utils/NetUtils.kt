package com.yingyuzhang.iptv.utils

import android.R.attr.host
import android.content.Context
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit


class NetUtils {

    fun isIPv6Supported(context: Context?, downloadCallback: DownloadCallback) {
        val address = InetAddress.getByName(null)
        println("IP: " + address.hostAddress)
        when (address.address.size) {
            4 -> println("根据byte数组长度判断这个IP地址是IPv4地址!")
            16 -> println("根据byte数组长度判断这个IP地址是IPv6地址!")
        }
        if (address is Inet4Address) {
            Toast.makeText(context, "当前是IPv4地址部分功能使用受限", Toast.LENGTH_SHORT)
            downloadCallback.onDownloadSuccess("IPv4")
        } else if (address is Inet6Address)
//            Toast.makeText(context, "IPv6连接成功！", Toast.LENGTH_SHORT).show()
            isIPv6Available(object : DownloadCallback {
                override fun onDownloadFailure(exception: Exception) {
                    downloadCallback.onDownloadFailure(exception)
                }

                override fun onDownloadSuccess(file: String) {
                    downloadCallback.onDownloadSuccess(file)
                }
            })
    }
//http://ipv6.ipv6-test.ch/ip/?callback=?


    fun isIPv6Available(callback: DownloadCallback) {
        Thread {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间
                .build()

            val request = Request.Builder()
                .url("http://ipv6.ipv6-test.ch/ip/?callback=?")
                .build()

            try {
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonBody = response.body?.string()?.trim()
                    val jsonCallback = jsonBody?.removeSurrounding("callback(", ")")
                    val jsonObject = JSONObject(jsonCallback)
                    if (jsonObject?.optString("type") == "ipv6")
                        callback.onDownloadSuccess(jsonObject?.optString("ip").toString())
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback.onDownloadFailure(e)
            }
        }.start()
    }
}