package com.example.customview.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.customview.R
import kotlinx.coroutines.*

/**
 * 自定义组件，刮刮乐的实现
 */
class ScratchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    //底部图片
    private var mBgBm: Bitmap? = null

    //灰色蒙层图片
    private lateinit var mGrayBm: Bitmap

    //灰色蒙层画布
    private lateinit var mGrayCanvas: Canvas

    //灰色蒙层画笔
    private lateinit var mGrayPaint: Paint

    //手指滑动路径
    private lateinit var mTouchPath: Path

    //手指滑动路径画笔
    private lateinit var mTouchPaint: Paint

    //组件的高宽
    private var mWidth: Int = 0
    private var mHeight: Int = 0

    //手指移动时的坐标
    private var mMoveX: Float = 0.0f
    private var mMoveY: Float = 0.0f

    //是否初始化完毕
    private var isInit = false
    //是否划出结果
    private var hasFinish = false
    //已划出来的面积
    private var mScrathArea: Float = 0.0f

    //蒙层总面积
    private val totalArea: Int by lazy { mWidth * mHeight }

    //底层图片ID
    private var resId: Int = -1
    //路径粗细
    private var mScratchRadius: Int = 0
    //蒙层颜色
    private var mMarkColor: Int = 0


    init {
        //关闭硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView)
        resId = typeArray.getResourceId(R.styleable.ScratchView_scratch_drawable, -1)
        mScratchRadius =
            typeArray.getDimensionPixelOffset(R.styleable.ScratchView_scratch_radius, 40)
        mMarkColor = typeArray.getColor(R.styleable.ScratchView_scratch_mark_color, Color.LTGRAY)
        typeArray.recycle()

        if (resId != -1) {
            //获取底部图片资源
            mBgBm = BitmapFactory.decodeResource(resources, R.drawable.bg_scratch)
        }

        //初始化蒙层画笔
        mGrayPaint = Paint()
        mGrayPaint.color = mMarkColor

        //初始化路径和路径画笔
        mTouchPath = Path()
        mTouchPaint = Paint()

        mTouchPaint.color = mMarkColor
        //描边
        mTouchPaint.style = Paint.Style.STROKE
        //宽度值
        mTouchPaint.strokeWidth = mScratchRadius.toFloat()
        //线段连接处
        mTouchPaint.strokeJoin = Paint.Join.ROUND
        //混合操作,相交处透明
        mTouchPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)

        //开启协程，检测是否画出了主界面
        checkFinish()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (resId != -1) {
            //如果设置有资源，则显示区域根据图片高宽设定
            setMeasuredDimension(mBgBm!!.width, mBgBm!!.height)
        }
    }


    private fun checkFinish() {
        //启动协程，由于需要不断运行，不能放在主线程
        launch(Dispatchers.Default) {
            while (!hasFinish) {
                delay(1000)
                for (i in 0 until mWidth) {
                    for (j in 0 until mHeight) {
                        //透明处的像素点为0
                        if (0 == mGrayBm.getPixel(i, j)) {
                            mScrathArea++
                        }
                    }
                }
                //半段面积是否超过门限
                if (mScrathArea / totalArea > 0.6f) {
                    hasFinish = true
                    //切换到主线程
                    withContext(Dispatchers.Main) {
                        mScratchListener?.finish()
                    }
                }
                mScrathArea = 0.0f
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (resId != -1) {
            //先绘制底部图片
            canvas?.drawBitmap(mBgBm!!, 0f, 0f, null)
        }
        //绘制蒙层图片
        canvas?.drawBitmap(mGrayBm, 0f, 0f, mGrayPaint)

        //在蒙层上绘制手势，来将手势划过的地方变为透明
        //混合模式目标图像
        mGrayCanvas.drawRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), mGrayPaint)
        //混合模式源图像
        mGrayCanvas.drawPath(mTouchPath, mTouchPaint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = right - left
        mHeight = bottom - top

        initGrayBm()
        isInit = true
    }

    //初始化蒙层图
    private fun initGrayBm() {
        mGrayBm = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
        mGrayCanvas = Canvas(mGrayBm)
        mGrayCanvas.drawColor(mMarkColor)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //取消协程
        cancel()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mMoveX = event.x
                mMoveY = event.y
                mTouchPath.moveTo(mMoveX, mMoveY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = event.x
                val endY = event.y
                Log.i(TAG, "ontouchevent: " + (endX - mMoveX).toString())
                mTouchPath.quadTo(
                    (endX - mMoveX) / 2 + mMoveX,
                    (endY - mMoveY) / 2 + mMoveY,
                    mMoveX,
                    mMoveY
                )
                mMoveX = endX
                mMoveY = endY
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private var mScratchListener: ScratchListener? = null

    fun setScratchListener(listener: ScratchListener) {
        mScratchListener = listener
    }

    interface ScratchListener {
        fun finish()
    }

    companion object {
        const val TAG = "ScratchView"
    }
}