package com.example.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.customview.widget.ScratchView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scratch_view.setScratchListener(object : ScratchView.ScratchListener {
            override fun finish() {
                tip_tv.visibility = View.VISIBLE
            }

        })
    }
}
