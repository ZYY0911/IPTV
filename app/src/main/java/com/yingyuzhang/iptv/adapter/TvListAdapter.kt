package com.yingyuzhang.iptv.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yingyuzhang.iptv.R
import com.yingyuzhang.iptv.base.AppAdapter
import com.yingyuzhang.iptv.base.BaseAdapter
import com.yingyuzhang.iptv.bean.TvList

class TvListAdapter constructor(context: Context) : AppAdapter<TvList>(context) {

    var onClickItemInterface: OnClickItemInterface<TvList>? = null
    var indexCurrent = 0

    fun selectChange(newIndex: Int) {
        val temp = indexCurrent
        indexCurrent = newIndex
        notifyItemChanged(temp)
        notifyItemChanged(indexCurrent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder()
    }

    inner class ViewHolder : AppViewHolder(R.layout.tv_list_item) {
        private val itemPng: ImageView? by lazy { findViewById(R.id.item_png) }
        private val itemName: TextView? by lazy { findViewById(R.id.item_name) }
        private val itemNum: TextView? by lazy { findViewById(R.id.item_num) }
        override fun onBindView(position: Int) {
//            val item = getItem(position)
            getItem(position).apply {
                itemPng?.let {
                    Glide.with(getContext())
//                        .load(item?.tvgLogo)
                        .load(this.tvgLogo)
                        .placeholder(R.mipmap.tupianjiazaishibai)
                        .into(it)
                }
                itemName?.text = this.tvName
                itemView.setOnClickListener {
                    selectChange(position)
                    if (null != onClickItemInterface) onClickItemInterface!!.onClickItem(this)
                }
                itemNum?.text = "${this.id + 1}"
            }
        }
    }

    override fun generateDefaultLayoutManager(context: Context): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 5)
    }
}