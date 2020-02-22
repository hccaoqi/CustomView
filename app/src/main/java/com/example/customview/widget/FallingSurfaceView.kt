package com.example.customview.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.graphics.contains
import com.example.customview.R
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.Runnable
import java.util.*
import kotlin.concurrent.thread

/**
 * 红包雨
 */
class FallingSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback,
    CoroutineScope by MainScope() {


    /**
     * 画笔
     */
    val paint = Paint()

    /**
     * 红包视图
     */
    var mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_red_package)
    /**
     * 红包位图宽度
     */
    private var bitmapWidth = 0
    private var bitmapHeight = 0

    private var surfaceHolder = holder

    /**
     * 当前生成的红包数量
     */
    private var curGenerateCount: Int = 0
    /**
     * 生成的红包总数
     */
    private var maxCount = 100

    /**
     * 间隔期，用于控制红包雨的密度
     */
    private var mDensity = 10

    /**
     * 当前间隔期
     */
    private var curIndex = 0


    /**
     * 掉落对象集合
     */
    private var fallingItems = Collections.synchronizedList(arrayListOf<FallingItem>())

    /**
     * 上一次红包的X坐标
     */
    private var lastStartX = 0

    private val random = Random()

    private var mCanvasWidth = 0
    private var mCanvasHeight = 0

    private var mFlag = false

    private var mMatrix = Matrix()

    private var onceTime = 0

    init {

        paint.isAntiAlias = true

        bitmapWidth = mBitmap.width
        bitmapHeight = mBitmap.height

        surfaceHolder.addCallback(this)
        setZOrderOnTop(true)

        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT)
    }


    /**
     * 添加红包
     */
    private fun addItem() {
        if (curGenerateCount > maxCount) {
            return
        }
        curIndex++
        if (curIndex % mDensity != 0) {
            return
        }

        var fallingItem = FallingItem()
        var startInLeft = 0
        if (lastStartX > bitmapWidth) {
            //以0为起点，以上一个红包的左边缘偏移一个位图为终点，这个范围内的随机值
            startInLeft = random.nextInt(lastStartX - bitmapWidth)
        }

        var startInRight = 0
        if (lastStartX < mCanvasWidth - bitmapWidth + 1) {
            //以上一个红包的右边缘偏一个像素点为起点，以画布右边减去一个位图宽度为终点
            startInRight = random.nextInt(mCanvasWidth - lastStartX - bitmapWidth + 1) + lastStartX
        }

        //设置横坐标
        if (startInLeft > 0 && startInRight > 0) {
            fallingItem.startX = if (random.nextBoolean()) startInLeft else startInRight
        } else {
            fallingItem.startX = if (startInLeft == 0) startInRight else startInLeft
        }

        //设置其余属性
        fallingItem.startY = -60
        fallingItem.rotate = random.nextInt(360)
        fallingItem.speed = (random.nextInt(3) + 2) * 5
        lastStartX = fallingItem.startX
        fallingItems.add(fallingItem)
        curGenerateCount++

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                checkInRect(event)
            }
        }
        return true
    }

    private var count = 0
    private fun checkInRect(event: MotionEvent) {
        val x = event.x
        val y = event.y
        synchronized(fallingItems) {
            for (i in 0 until fallingItems.size) {
                val item = fallingItems[i]
                val rect = Rect(
                    item.startX,
                    item.startY,
                    item.startX + bitmapWidth,
                    item.startY + bitmapHeight
                )
                if (rect.contains(x.toInt(), y.toInt())) {
                    count++
                    resetMoveItems(item)
                    break
                }
            }
        }
    }

    private fun resetMoveItems(item: FallingItem) {
        item.startX = 0
        item.startY = -100
        if (fallingItems.contains(item)) {
            fallingItems.remove(item)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mFlag = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.i("caoqi", "surfaceCreated")
        mFlag = true
//        thread { draw() }
//        Thread(this).start()
        launch(Dispatchers.Default) {
            Log.i("caoqi ", Thread.currentThread().name)
            draw()
        }
    }


    fun draw() {
        Log.i("caoqi", "run")
        var canvas: Canvas? = null
        var items: FallingItem? = null
        while (mFlag) {
            val startTime = System.currentTimeMillis()
            try {
                canvas = surfaceHolder.lockCanvas()
                if (mCanvasHeight == 0) {
                    mCanvasWidth = canvas.width
                    mCanvasHeight = canvas.height
                }

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            } catch (exception: Exception) {
                break
            }
//            fallingItems.forEach {
//                mMatrix.setRotate(it.rotate.toFloat(), bitmapWidth / 2f, bitmapHeight / 2f)
//                mMatrix.postTranslate(it.startX.toFloat(), it.startY.toFloat())
//                canvas.drawBitmap(mBitmap, mMatrix, paint)
//                it.startY = it.startY + it.speed
//            }
            synchronized(fallingItems) {
                for (i in 0 until fallingItems.size) {
                    val items = fallingItems[i]
                    mMatrix.setRotate(items.rotate.toFloat(), bitmapWidth / 2f, bitmapHeight / 2f)
                    mMatrix.postTranslate(items.startX.toFloat(), items.startY.toFloat())
                    canvas.drawBitmap(mBitmap, mMatrix, paint)
                    items.startY = items.startY + items.speed
                }
            }
            //解锁画布
            surfaceHolder.unlockCanvasAndPost(canvas)

            //添加坠落对象
            addItem()

            //屏幕上最多出现50个红包
            if (fallingItems.size > 50) {
                fallingItems.removeAt(0)
            }
            onceTime = (System.currentTimeMillis() - startTime).toInt()

        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }


    private data class FallingItem(
        var startX: Int = 0,
        var startY: Int = 0,
        var speed: Int = 0,
        var rotate: Int = 0
    )

//    override fun run() {
//        draw()
//    }
}