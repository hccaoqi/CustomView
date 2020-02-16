package com.example.customview.widget

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.OvershootInterpolator
import com.example.customview.R
import kotlin.math.sqrt

/**
 * 圆环式弹出菜单
 */
class RoundelMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    //中心圆画笔
    private var mCenterPaint: Paint = Paint()
    //中心圆展开时颜色
    private var mCenterColor: Int = Color.parseColor("#ffff8800")

    //收缩圆画笔
    private var mRoundPaint: Paint = Paint()
    //收缩圆展开是的颜色
    private var mRoundColor: Int = Color.parseColor("#ffffbb33")

    //收缩圆的扩展动画
    private lateinit var mExpandAnimator: ValueAnimator
    //收缩进度
    private var expandProgress = 0.0f

    //收缩圆的颜色变化
    private lateinit var mColorAnimator: ValueAnimator

    //圆心坐标
    private var center: PointF = PointF()

    //中心圆半径
    private var collapseRadius: Int = 0
    //扩展圆半径
    private var expandRadius: Int = 0

    //菜单子项的宽度
    private var mItemWidth = 0

    //当前状态
    private var currentState = STATE_COLLAPSE

    //展开或收缩动画时长
    private var mDuration = 0

    //子view间的间隔
    private var mItemAnimIntervalTime = 0

    private lateinit var mCenterDrawable: Drawable

    private val typedArray: TypedArray by lazy {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundelMenu)
        typedArray
    }

    init {
        //从用户设置的xml中读取属性
        initAttrs()

        //初始化画笔
        initPaint()
        //自定义viewGroup需要设置此参数，否则invalidate时不会触发onDraw
        setWillNotDraw(false)

        mCenterDrawable = resources.getDrawable(R.drawable.ic_menu, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //设置阴影
            elevation = 8F
        }
        initAnimation()

    }

    private fun initAttrs() {
        collapseRadius = typedArray.getDimensionPixelSize(
            R.styleable.RoundelMenu_round_menu_collapseRadius,
            dp2px(DEFAULT_COLLAPSERADUIS)
        )
        expandRadius = typedArray.getDimensionPixelSize(
            R.styleable.RoundelMenu_round_menu_expandRadius,
            dp2px(DEFAULT_EXPANDRADUIS)
        )
        mRoundColor = typedArray.getColor(
            R.styleable.RoundelMenu_round_menu_roundColor,
            Color.parseColor("#ffffbb33")
        )
        mCenterColor = typedArray.getColor(
            R.styleable.RoundelMenu_round_menu_centerColor, Color.parseColor("#ffff8800")
        )

        mDuration =
            typedArray.getInteger(R.styleable.RoundelMenu_round_menu_duration, DEFAULT_DURATION)
        mItemAnimIntervalTime =
            typedArray.getInteger(
                R.styleable.RoundelMenu_round_menu_anim_delay,
                DEFAULT_INTERVAL_TIME
            )
        mItemWidth = typedArray.getDimensionPixelSize(
            R.styleable.RoundelMenu_round_menu_item_width,
            dp2px(DEFAULT_COLLAPSERADUIS)
        )
        typedArray.recycle()
    }

    private fun initPaint() {
        //初始化中心圆画笔
        mCenterPaint.color = mRoundColor
        mCenterPaint.style = Paint.Style.FILL
        mCenterPaint.isAntiAlias = true

        //初始化收缩圆画笔
        mRoundPaint.color = mRoundColor
        mRoundPaint.style = Paint.Style.FILL
        mRoundPaint.isAntiAlias = true
    }

    //初始化动画
    private fun initAnimation() {
        //设置展开动画
        mExpandAnimator = ValueAnimator.ofFloat(0F, 1F)
        mExpandAnimator.interpolator = OvershootInterpolator()
        mExpandAnimator.duration = mDuration.toLong()
        mExpandAnimator.addUpdateListener {
            expandProgress = it.animatedValue as Float
            mRoundPaint.alpha = (expandProgress * 255).toInt()
            invalidate()
        }

        //设置展开是颜色变化
        mColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), mRoundColor, mCenterColor)
        mColorAnimator.duration = mDuration.toLong()
        mColorAnimator.addUpdateListener {
            mCenterPaint.color = it.animatedValue as Int
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) {
            return
        }
        calculateMenuItemPositon()
        for (i in 0 until childCount) {
            val item = getChildAt(i)
            item.layout(
                (l + item.x).toInt(),
                (t + item.y).toInt(),
                (l + item.x + item.measuredWidth).toInt(),
                (t + item.y + item.measuredHeight).toInt()
            )
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {

            //初始化加载时，设置菜单项不可见
            getChildAt(i).apply {
                this.visibility = View.GONE
                this.alpha = 0F
                this.scaleX = 1F
                this.scaleY = 1F
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val x = w / 2
        val y = h / 2
        center.set(x.toFloat(), y.toFloat())

        //中心图片设置padding为10dp
        mCenterDrawable.setBounds(
            (center.x - (collapseRadius - dp2px(10))).toInt(),
            (center.y - (collapseRadius - dp2px(10))).toInt(),
            (center.x + (collapseRadius - dp2px(10))).toInt(),
            (center.y + (collapseRadius - dp2px(10))).toInt()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            outlineProvider = OvalOutLine()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //绘制扩展圆
        if (expandProgress > 0) {
            canvas?.drawCircle(
                center.x,
                center.y,
                collapseRadius + (expandRadius - collapseRadius) * expandProgress,
                mRoundPaint
            )
        }

        //绘制中心圆
        canvas?.drawCircle(center.x, center.y, collapseRadius.toFloat(), mCenterPaint)

        val count = canvas?.saveLayer(0F, 0F, width.toFloat(), height.toFloat(), null) ?: 0
        canvas?.rotate(45 * expandProgress, center.x, center.y)
        mCenterDrawable.draw(canvas!!)
        canvas.restoreToCount(count)

    }


    private fun calculateMenuItemPositon() {
        val itemRadius = (expandRadius + collapseRadius) / 2
        val area = RectF(
            center.x - itemRadius,
            center.y - itemRadius,
            center.x + itemRadius,
            center.y + itemRadius
        )

        val path = Path()
        path.addArc(area, 0f, 360f)
        val measure = PathMeasure(path, false)
        val len = measure.length
        val divisor = childCount
        val divider = len / divisor

        for (i in 0 until childCount) {
            val itemPoints = FloatArray(2)
            measure.getPosTan(i * divider + divider * 0.5f, itemPoints, null)
            val item = getChildAt(i)
            item.x = itemPoints[0] - mItemWidth / 2
            item.y = itemPoints[1] - mItemWidth / 2
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchPoint = PointF()
        touchPoint.set(event?.x ?: 0.0f, event?.y ?: 0.0f)
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val distance = getPositionDistance(touchPoint, center)
                //收缩状态，则展开
                if (currentState == STATE_COLLAPSE) {
                    if (distance < collapseRadius) {
                        expand()
                        return true
                    }
                    return false
                } else {
                    if (distance > (collapseRadius + (expandRadius - collapseRadius) * expandProgress)
                        || distance < collapseRadius
                    ) {
                        collapse()
                        return true
                    }
                    return false
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun collapse() {
        currentState = STATE_COLLAPSE
//        for (i in 0 until childCount) {
//            getChildAt(i).visibility = View.GONE
//        }
        invalidate()
        startCollapseAnimation()
    }

    private fun expand() {
        currentState = STATE_EXPAND
        for (i in 0 until childCount) {
            getChildAt(i).visibility = View.VISIBLE
        }
        invalidate()
        startExpandAnimation()
    }

    /**
     * 展开动画
     */
    private fun startExpandAnimation() {
        mExpandAnimator.setFloatValues(expandProgress, 1f)
        mExpandAnimator.start()

        mColorAnimator.setObjectValues(mColorAnimator.animatedValue ?: mRoundColor, mCenterColor)
        mColorAnimator.start()

        var delay = mItemAnimIntervalTime
        for (i in 0 until childCount) {
            getChildAt(i).animate().apply {
                startDelay = delay.toLong()
                duration = mDuration.toLong()
                scaleX(1F)
                scaleY(1F)
                alpha(1F)
            }.start()
            delay += mItemAnimIntervalTime
        }
    }


    /**
     * 收缩动画
     */
    private fun startCollapseAnimation() {
        mExpandAnimator.setFloatValues(expandProgress, 0F)
        mExpandAnimator.start()

        mColorAnimator.setObjectValues(mColorAnimator.animatedValue ?: mCenterColor, mRoundColor)
        mColorAnimator.start()


        var delay = mItemAnimIntervalTime
        for (i in childCount - 1 downTo 0) {
            getChildAt(i).animate().apply {
                startDelay = delay.toLong()
                duration = mDuration.toLong()
                alpha(0F)
                scaleY(0F)
                scaleX(0F)
            }.start()
            delay += mItemAnimIntervalTime
        }
    }


    private fun dp2px(dpVal: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        //收缩状态
        private const val STATE_COLLAPSE = 0
        //展开状态
        private const val STATE_EXPAND = 1
        //默认动画时长
        private const val DEFAULT_DURATION = 1500
        //默认时间间隔
        private const val DEFAULT_INTERVAL_TIME = 200
        //默认内圈半径
        private const val DEFAULT_COLLAPSERADUIS = 22
        //默认外圈半径
        private const val DEFAULT_EXPANDRADUIS = 84

        private fun getPositionDistance(touchPointF: PointF, center: PointF): Double {
            val dx = touchPointF.x - center.x
            val dy = touchPointF.y - center.y
            return sqrt((dx * dx + dy * dy).toDouble())
        }
    }

    /**
     * 内部类，用于设置阴影
     */
    inner class OvalOutLine : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            val radius = collapseRadius + (expandRadius - collapseRadius) * expandProgress
            val rect = Rect(
                (center.x - radius).toInt(),
                (center.y - radius).toInt(),
                (center.x + radius).toInt(),
                (center.y + radius).toInt()
            )
            outline?.setRoundRect(rect, radius)
        }

    }
}