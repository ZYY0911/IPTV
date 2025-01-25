package com.yingyuzhang.iptv.utils

import android.content.Context
import android.util.Xml
import com.yingyuzhang.iptv.App
import com.yingyuzhang.iptv.bean.ForeShow
import com.yingyuzhang.iptv.bean.TvList
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FileUtils {
    fun downloadFile(url: String, name: String, context: Context, callback: DownloadCallback) {
        Thread {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder()
                    .url(url)
                    .build()
                // 发起请求并处理响应
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    // 获取响应体
                    val responseBody = response.body ?: throw IOException("Response body is null")

                    // 创建本地文件
                    val localFileDir = File(context.cacheDir.absolutePath)
                    if (!localFileDir.exists()) {
                        localFileDir.mkdirs()
                    }
                    val localFile = File(localFileDir, name)

                    // 将响应体写入文件
                    FileOutputStream(localFile).use { output ->
                        responseBody.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    callback.onDownloadSuccess(localFile.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                下载失败，请到设置中手动更新"
                callback.onDownloadFailure(e)
            }
        }.start()
    }

    fun existList(context: Context, fileName: String): Boolean {
        val localFile = File(context.cacheDir.absolutePath + File.separator + fileName)
        return localFile.exists()
    }


    fun parseTvListFromFile(context: Context) {
        var currentExtinf = ""
        var currentTvgName = ""
        var currentTvgLogo = ""
        var currentGroupTitle = ""
        var currentTvName = ""
        var currentTvUrl = ""
        val filePath = File(context.cacheDir.absolutePath + File.separator + App.getTvFileName())
        if (filePath.exists()) {
            val lines = File(filePath.absolutePath).readLines()
            var index = 0
            for (line in lines) {
                when {
                    line.startsWith("#EXTM3U") -> {
                        val tvgUrl = Regex("x-tvg-url=\"([^\"]+)\"").find(line)
                        //TODO
//                        App.setTvShowTime(tvgUrl?.groups?.get(1)?.value.orEmpty())
                    }

                    line.startsWith("#EXTINF:") -> {
                        currentExtinf = line.substringAfter("#EXTINF:").trim()
                        val tvgNameMatch = Regex("tvg-name=\"([^\"]+)\"").find(line)
                        val tvgLogoMatch = Regex("tvg-logo=\"([^\"]+)\"").find(line)
                        val groupTitleMatch = Regex("group-title=\"([^\"]+)\"").find(line)

                        currentTvgName = tvgNameMatch?.groups?.get(1)?.value.orEmpty()
                        currentTvgLogo = tvgLogoMatch?.groups?.get(1)?.value.orEmpty()
                        currentGroupTitle = groupTitleMatch?.groups?.get(1)?.value.orEmpty()
                    }

                    line.startsWith("http://") || line.startsWith("https://") -> {
                        currentTvUrl = line.trim()
                        // The tvName is extracted from EXTINF info
                        currentTvName = currentExtinf.substringAfterLast(",").trim()
                        // Create TvList instance and add to list
                        App.getTvList().add(
                            TvList(
                                id = index,
                                EXTINF = currentExtinf,
                                tvgName = currentTvgName,
                                tvgLogo = currentTvgLogo,
                                groupTitle = currentGroupTitle,
                                tvName = currentTvName,
                                tvUrl = currentTvUrl
                            )
                        )
                        // Reset URL for next item
                        currentTvUrl = ""
                        index++
                    }
                }
            }
        }
    }


    fun parseXmlFromCache(context: Context, channelName: String): String {
        try {
            val cacheDir: File = context.cacheDir
            val file = File(cacheDir, "e.xml")
            val inputStream = FileInputStream(file)

            val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
            val parser: XmlPullParser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault())
            val currentDate = Date()

            var eventType = parser.eventType
            var currentChannel: String? = null
            var currentTitle: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (name) {
//                            "channel" -> {
//                                currentChannel = parser.getAttributeValue(null, "id")
//                            }
//
//                            "display-name" -> {
//                                if (currentChannel != null) {
//                                    val lang = parser.getAttributeValue(null, "lang")
//                                    val displayName = parser.nextText()
//                                }
//                            }

                            "programme" -> {
                                val channel = parser.getAttributeValue(null, "channel")
                                if (channel == channelName) {
                                    val start = parser.getAttributeValue(null, "start")
                                    val stop = parser.getAttributeValue(null, "stop")

                                    val startDate = dateFormat.parse(start)
                                    val stopDate = dateFormat.parse(stop)

                                    if (currentDate.after(startDate) && currentDate.before(stopDate)
                                    ) {
                                        parser.next()
                                        parser.next()
//                                        parser.nextText()
                                        println("Current time is between start and stop for channel: $channel")
                                        val lang = parser.getAttributeValue(null, "lang")
                                        if (lang == "zh") {
                                            currentTitle = parser.nextText()
                                            println("Programme Title: $currentTitle")
                                            inputStream.close()
                                            return currentTitle
                                        }
                                    }
                                }
                            }

//                            "title" -> {
//                                if (currentChannel != null) {
//                                    val lang = parser.getAttributeValue(null, "lang")
//                                    if (lang == "zh") {
//                                        currentTitle = parser.nextText()
//                                        println("Programme Title: $currentTitle")
//                                    }
//                                }
//                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (name == "channel") {
                            currentChannel = null
                        }
                        if (name == "programme") {
                            currentTitle = null
                        }
                    }
                }
                eventType = parser.next()
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "暂无数据源"
    }

    fun parseXmlToProgrammes(context: Context): ArrayList<ForeShow> {
        val programmes = ArrayList<ForeShow>()
        val cacheDir: File = context.cacheDir
        val file = File(cacheDir, "e.xml")
        val inputStream = FileInputStream(file)

        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        val parser: XmlPullParser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentProgramme: ForeShow? = null
        var currentText = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "programme") {
                        val channel = parser.getAttributeValue(null, "channel")
                        val start = parser.getAttributeValue(null, "start")
                        val stop = parser.getAttributeValue(null, "stop")
                        currentProgramme = ForeShow(channel, start, stop, "")
                    }
                }
                XmlPullParser.TEXT -> {
                    currentText = parser.text
                }
                XmlPullParser.END_TAG -> {
                    if (currentProgramme != null) {
                        when (tagName) {
                            "title" -> currentProgramme = currentProgramme.copy(title = currentText)
                            "programme" -> programmes.add(currentProgramme)
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return programmes
    }

    fun clearCache(context: Context) {
        val cacheDir = context.cacheDir
        deleteFilesInDirectory(cacheDir)
    }
    private fun deleteFilesInDirectory(directory: File): Boolean {
        if (directory.isDirectory) {
            val children = directory.list()
            for (i in children.indices) {
                val success = deleteFilesInDirectory(File(directory, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return directory.delete()
    }
}