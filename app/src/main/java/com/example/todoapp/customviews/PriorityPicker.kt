package com.example.todoapp.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import com.example.todoapp.R
import com.example.todoapp.tasks.TaskPriority


class PriorityPicker : AppCompatSeekBar {

    private var colors: ArrayList<Int> = arrayListOf(Color.RED, Color.YELLOW, Color.GREEN)
    private val w = getPixelValueFromDP(16f) // Width of color swatch
    private val h = getPixelValueFromDP(16f) // Height of color swatch
    private val halfW = if (w >= 0) w / 2f else 1f
    private val halfH = if (h >= 0) h / 2f else 1f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = getPixelValueFromDP(12f)
    }

    private var noColorDrawable: Drawable? = null
        set(value) {
            w2 = value?.intrinsicWidth ?: 0
            h2 = value?.intrinsicHeight ?: 0
            halfW2 = if (w2 >= 0) w2 / 2 else 1
            halfH2 = if (h2 >= 0) h2 / 2 else 1
            value?.setBounds(-halfW2, -halfH2, halfW2, halfH2)
            field = value
        }
    var w2 = 0
    private var h2 = 0
    private var halfW2 = 1
    private var halfH2 = 1

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes

        val a = context.obtainStyledAttributes(
            attrs, R.styleable.PriorityPicker, defStyle, 0
        )
        try {
            if (a.hasValue(R.styleable.PriorityPicker_colors)) {
                colors = a.getTextArray(R.styleable.PriorityPicker_colors)
                    .map {
                        Color.parseColor(it.toString())
                    } as ArrayList<Int>
            }
        } finally {
            a.recycle()
        }
        colors.add(0, android.R.color.transparent)
        max = colors.size - 1
        progressBackgroundTintList = ContextCompat.getColorStateList(
            context,
            android.R.color.transparent
        )
        progressTintList = ContextCompat.getColorStateList(
            context,
            android.R.color.transparent
        )
        splitTrack = false
        setPadding(
            paddingLeft,
            paddingTop,
            paddingRight,
            paddingBottom + getPixelValueFromDP(32f).toInt()
        )
        thumb = context.getDrawable(R.drawable.ic_color_slider_thumb)
        noColorDrawable = context.getDrawable(R.drawable.ic_no_color)
        noColorDrawable?.callback = this

        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                listeners.forEach {
                    it(colors[progress])
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    var selectedColorValue: Int = android.R.color.transparent
        set(value) {
            val index = colors.indexOf(value)
            progress = if (index == -1) {
                0
            } else {
                index
            }
        }

    private var listeners: ArrayList<(Int) -> Unit> = arrayListOf()

    fun addListener(function: (Int) -> Unit) {
        listeners.add(function)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawTickMarks(canvas)
    }

    private fun drawTickMarks(canvas: Canvas?) {
        canvas?.let {
            val count = colors.size
            val saveCount = canvas.save()
            canvas.translate(
                paddingLeft.toFloat(),
                (height / 2).toFloat() + getPixelValueFromDP(16f)
            )
            if (count > 1) {
                val spacing = (width - paddingLeft - paddingRight) / (count - 1).toFloat()
                for (i in 0 until count) {
                    val label = resources.getString(TaskPriority.values()[i].label)
                    if (i == 0) {
                        noColorDrawable?.draw(canvas)
                    } else {
                        paint.color = colors[i]
                        canvas.drawRect(
                            -halfW, -halfH,
                            halfW, halfH, paint
                        )
                    }
                    paint.color = Color.BLACK
                    canvas.drawText(label, 0f, (1.5 * h).toFloat(), paint)
                    canvas.translate(spacing, 0f)
                }
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private fun getPixelValueFromDP(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            context.resources.displayMetrics
        )
    }
}