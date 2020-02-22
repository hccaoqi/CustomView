package com.example.customview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BaguaView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //画笔
    val paint = Paint()

    var size: Int = 0
        get() = if (measuredHeight < measuredWidth) measuredHeight else measuredWidth


    private var xCenter = 0f
    private var yCenter = 0f


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        xCenter = (right - left) / 2f
        yCenter = (bottom - top) / 2f

        paint.color = Color.YELLOW
        canvas?.drawCircle(xCenter, yCenter, size / 2f, paint)

        paint.color = Color.BLACK
//        canvas?.drawArc(
//            size * 0.25f,
//            size * 0.25f,
//            size * 0.75f,
//            size * 0.75f,
//            -90f,
//            180f,
//            false, paint
//        )

        canvas?.drawCircle(xCenter, yCenter * 1.25f, size / 8f, paint)

        paint.color = Color.YELLOW
        canvas?.drawCircle(xCenter, yCenter * 0.75f, size / 8f, paint)

        canvas?.drawCircle(xCenter, yCenter * 1.25f, size / 32f, paint)
        paint.color = Color.BLACK
        canvas?.drawCircle(xCenter, yCenter * 0.75f, size / 32f, paint)

    }



    private val threeLineList = arrayListOf<List<Boolean>>().apply {
        add(arrayListOf(false, false, false))
        add(arrayListOf(true, false, false))
        add(arrayListOf(true, false, true))
        add(arrayListOf(true, true, false))

        add(arrayListOf(true, true, true))
        add(arrayListOf(false, true, true))
        add(arrayListOf(false, true, false))
        add(arrayListOf(false, false, true))
    }
}