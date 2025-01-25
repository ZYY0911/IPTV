package com.yingyuzhang.iptv.activity

import androidx.constraintlayout.widget.ConstraintLayout
import com.yingyuzhang.iptv.base.BaseActivity
import com.yingyuzhang.iptv.databinding.ActivityRadioBinding

class RadioActivity : BaseActivity() {

    private lateinit var binding: ActivityRadioBinding
    override fun setView(): ConstraintLayout {
        binding = ActivityRadioBinding.inflate(layoutInflater)
        return binding.root
    }

}