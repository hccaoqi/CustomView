package com.example.customview.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.customview.R
import com.example.customview.widget.ScratchView
import kotlinx.android.synthetic.main.activity_scratch_view.*

class ScratchViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scratch_view)

        scratch_view.setScratchListener(object :ScratchView.ScratchListener{
            override fun finish() {
                tip_tv.visibility = View.VISIBLE

            }
        })

        text_view.setOnClickListener {
            Toast.makeText(applicationContext, "点击了布局结果", Toast.LENGTH_SHORT).show()
        }

    }

}
