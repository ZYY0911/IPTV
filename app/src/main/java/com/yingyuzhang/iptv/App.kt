package com.yingyuzhang.iptv

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.yingyuzhang.iptv.bean.TvList
import com.yingyuzhang.iptv.utils.SharedPreferencesUtil

//              佛祖保佑，永无BUG
//                        _oo0oo_
//                       o8888888o
//                       88" . "88
//                       (| -_- |)
//                       0\  =  /0
//                     ___/`---'\___
//                   .' \\|     |// '.
//                  / \\|||  :  |||// \
//                 / _||||| -:- |||||- \
//                |   | \\\  -  /// |   |
//                | \_|  ''\---/''  |_/ |
//                \  .-\__  '-'  ___/-. /
//              ___'. .'  /--.--\  `. .'___
//           ."" '<  `.___\_<|>_/___.' >' "".
//          | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//          \  \ `_.   \_ __\ /__ _/   .-` /  /
//      =====`-.____`.___ \_____/___.-`___.-'=====
//                        `=---='
//  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//              佛祖保佑       永无BUG
//
//       佛曰:
//             写字楼里写字间，写字间里程序员；
//             程序人员写程序，又拿程序换酒钱。
//             酒醒只在网上坐，酒醉还来网下眠；
//             酒醉酒醒日复日，网上网下年复年。
//             但愿老死电脑间，不愿鞠躬老板前；
//             奔驰宝马贵者趣，公交自行程序员。
//             别人笑我忒疯癫，我笑自己命太贱；
//             不见满街漂亮妹，哪个归得程序员？
class App : Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: Application? = null
        private var tvList = ArrayList<TvList>()

        //https://github.com/fanmingming/live/raw/main/tv/m3u/ipv6.m3u
        //http://192.168.50.207:8080/demo_war/ipv6.m3u
//    val tvListUrl = "https://live.fanmingming.com/tv/m3u/ipv6.m3u"
//    val tvListUrl = "https://live.fanmingming.com/tv/m3u/itv.m3u"
        private const val rootUrl = "https://live.fanmingming.cn"
        private var tvListUrl = ""
        private var tvFileName = ""
        private var tvShowTime = "$rootUrl/e.xml"

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun getTvList(): ArrayList<TvList> {
            return tvList
        }

        fun getTvFileName(): String {
            return tvFileName
        }

        fun getTvListUrl(): String {
            return tvListUrl
        }

        fun setTvShowTime(str: String) {
            tvShowTime = str
        }

        fun getTvShowTime(): String {
            return tvShowTime
        }

        fun restartApp(context: Context, targetActivity: Class<*>, extras: Bundle? = null) {
            tvList.clear()
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_IPV6)) {
                tvListUrl =
                    SharedPreferencesUtil.getString(SharedPreferencesUtil.TvListUrl6).toString()
                tvFileName =
                    SharedPreferencesUtil.getString(SharedPreferencesUtil.TvFileName6).toString()
            } else {
                tvListUrl =
                    SharedPreferencesUtil.getString(SharedPreferencesUtil.TvListUrl4).toString()
                tvFileName =
                    SharedPreferencesUtil.getString(SharedPreferencesUtil.TvFileName4).toString()
            }
            val intent = Intent(context, targetActivity)
            extras?.let { intent.putExtras(it) }
            context.startActivity(intent)
        }

    }


    override fun onCreate() {
        super.onCreate()
        SharedPreferencesUtil.init(this)
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.FIRST_INIT)) {
            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.FIRST_INIT, true)
            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.IS_IPV6, false)
            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.IS_AUTO_UPDATE, true)
            SharedPreferencesUtil.putString(
                SharedPreferencesUtil.TvListUrl6,
                "https://live.fanmingming.cn/tv/m3u/ipv6.m3u"
            )
            SharedPreferencesUtil.putString(SharedPreferencesUtil.TvFileName6, "ipv6.txt")
//            SharedPreferencesUtil.putString(SharedPreferencesUtil.TvListUrl4, "https://ghfast.top/https://raw.githubusercontent.com/YueChan/Live/main/APTV.m3u")
            SharedPreferencesUtil.putString(
                SharedPreferencesUtil.TvListUrl4,
                "https://mirror.ghproxy.com/https://raw.githubusercontent.com/YueChan/Live/main/APTV.m3u"
            )
            SharedPreferencesUtil.putString(SharedPreferencesUtil.TvFileName4, "APTV.txt")
            SharedPreferencesUtil.putString(SharedPreferencesUtil.SHOW_RATIO, "原始比例")
        }
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.IS_IPV6)) {
            tvListUrl =
                SharedPreferencesUtil.getString(SharedPreferencesUtil.TvListUrl6).toString()
            tvFileName =
                SharedPreferencesUtil.getString(SharedPreferencesUtil.TvFileName6).toString()
        } else {
            tvListUrl =
                SharedPreferencesUtil.getString(SharedPreferencesUtil.TvListUrl4).toString()
            tvFileName =
                SharedPreferencesUtil.getString(SharedPreferencesUtil.TvFileName4).toString()
        }

    }
}