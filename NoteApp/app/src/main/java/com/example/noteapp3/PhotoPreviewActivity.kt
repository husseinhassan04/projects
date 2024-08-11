package com.example.noteapp3

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PhotoPreviewActivity : AppCompatActivity(), View.OnTouchListener {
    private lateinit var imageView: ImageView
    private var matrix = Matrix()
    private var savedMatrix = Matrix()
    private val startPoint = PointF()
    private val MIN_ZOOM = 1f
    private val MAX_ZOOM = 3f
    private var startDistance = 0f
    private var midPoint = PointF()

    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2

    private var mode = NONE

    private lateinit var scaleGestureDetector: ScaleGestureDetector

    companion object {
        var imageBitmap: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)
        imageView = findViewById(R.id.imageView)

        // Set the bitmap from the companion object
        imageBitmap?.let {
            imageView.setImageBitmap(it)
        }

        imageView.setOnTouchListener(this)
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        // Wait for the layout to be complete before fitting the image
        imageView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                fitImageToView()
            }
        })
    }

    private fun fitImageToView() {
        val drawable = imageView.drawable ?: return
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val viewWidth = imageView.width
        val viewHeight = imageView.height

        val scaleX = viewWidth.toFloat() / imageWidth
        val scaleY = viewHeight.toFloat() / imageHeight
        val scale = Math.min(scaleX, scaleY)

        val dx = (viewWidth - imageWidth * scale) / 2
        val dy = (viewHeight - imageHeight * scale) / 2

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        imageView.imageMatrix = matrix
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                startDistance = distance(event)
                if (startDistance > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(midPoint, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(event.x - startPoint.x, event.y - startPoint.y)
                } else if (mode == ZOOM) {
                    val endDistance = distance(event)
                    if (endDistance > 10f) {
                        val scale = endDistance / startDistance
                        matrix.set(savedMatrix)
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }
        imageView.imageMatrix = matrix
        return true
    }

    private fun distance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM))
            matrix.setScale(scaleFactor, scaleFactor)
            imageView.imageMatrix = matrix
            return true
        }
    }
}
