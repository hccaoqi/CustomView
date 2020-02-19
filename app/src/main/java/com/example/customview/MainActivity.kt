package com.example.customview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customview.activity.ScratchViewActivity
import com.example.customview.adapter.EntranceItemAdapter
import com.example.customview.bean.ViewItemBean
import com.example.customview.widget.ScratchView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAdapter: EntranceItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewItemBeans = arrayListOf<ViewItemBean>()
        viewItemBeans.add(
            ViewItemBean(
                "ScratchView",
                "常见的刮刮乐效果",
                R.raw.scratch_view,
                ScratchViewActivity::class.java
            )
        )
        mAdapter = EntranceItemAdapter(this, viewItemBeans)
        view_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        view_rv.adapter = mAdapter


    }
}
