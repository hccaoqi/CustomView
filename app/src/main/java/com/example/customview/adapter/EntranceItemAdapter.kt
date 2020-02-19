package com.example.customview.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.customview.R
import com.example.customview.bean.ViewItemBean

class EntranceItemAdapter(
    private val mContext: Context,
    private val mViewItemList: MutableList<ViewItemBean>
) :
    RecyclerView.Adapter<EntranceItemAdapter.EntranceViewHolder>() {


    class EntranceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mRootView: CardView = itemView.findViewById(R.id.item_root_view)
        val mImageview: ImageView = itemView.findViewById(R.id.preview_iv)
        val mTitle: TextView = itemView.findViewById(R.id.view_name_tv)
        val mDesctible: TextView = itemView.findViewById(R.id.view_describe_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntranceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_entrance_item, parent, false)
        return EntranceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mViewItemList.size
    }

    override fun onBindViewHolder(holder: EntranceViewHolder, position: Int) {
        val mViewItemBean = mViewItemList[position]
        with(mViewItemBean) {
            holder.mTitle.text = this.title
            holder.mDesctible.text = this.describle
            holder.mImageview.setImageResource(this.priviewGif)
            holder.mRootView.setOnClickListener {
                mContext.startActivity(Intent(mContext, this.testClass))
            }
        }
    }
}