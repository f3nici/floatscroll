package com.floatscroll

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageButton
import android.widget.LinearLayout

class ScrollAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FloatScroll"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var touchSlop = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")

        SettingsStore.init(this)
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        createOverlay()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used — we only need gesture dispatch
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        Log.d(TAG, "Service destroyed")
    }

    private fun createOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val sizeDp = SettingsStore.getButtonSizeDp()
        val sizePx = dpToPx(sizeDp)

        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.overlay_background)
            alpha = SettingsStore.buttonOpacity

            val upButton = ImageButton(this@ScrollAccessibilityService).apply {
                setImageResource(R.drawable.ic_arrow_up)
                setBackgroundResource(R.drawable.circle_button_bg)
                contentDescription = "Scroll up"
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(2))
                }
                setOnClickListener {
                    if (!isDragging) scrollUp()
                }
            }

            val downButton = ImageButton(this@ScrollAccessibilityService).apply {
                setImageResource(R.drawable.ic_arrow_down)
                setBackgroundResource(R.drawable.circle_button_bg)
                contentDescription = "Scroll down"
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    setMargins(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(4))
                }
                setOnClickListener {
                    if (!isDragging) scrollDown()
                }
            }

            addView(upButton)
            addView(downButton)
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(16)
            y = dpToPx(200)
        }

        setupDragListener()

        try {
            windowManager?.addView(overlayView, layoutParams)
            Log.d(TAG, "Overlay added")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay", e)
        }
    }

    private fun setupDragListener() {
        overlayView?.setOnTouchListener { _, event ->
            val params = layoutParams ?: return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (dx * dx + dy * dy) > touchSlop * touchSlop) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager?.updateViewLayout(overlayView, params)
                    }
                    isDragging
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        isDragging = false
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    private fun scrollUp() {
        val distance = SettingsStore.scrollDistance
        updateOverlayAppearance()
        dispatchScrollGesture(isUp = true, distance = distance)
    }

    private fun scrollDown() {
        val distance = SettingsStore.scrollDistance
        updateOverlayAppearance()
        dispatchScrollGesture(isUp = false, distance = distance)
    }

    private fun dispatchScrollGesture(isUp: Boolean, distance: Int) {
        val displayMetrics = resources.displayMetrics
        val centerX = displayMetrics.widthPixels / 2f
        val centerY = displayMetrics.heightPixels / 2f
        val halfDistance = distance / 2f

        val path = Path()
        if (isUp) {
            // To scroll content UP, swipe DOWN (finger moves from top to bottom)
            path.moveTo(centerX, centerY - halfDistance)
            path.lineTo(centerX, centerY + halfDistance)
        } else {
            // To scroll content DOWN, swipe UP (finger moves from bottom to top)
            path.moveTo(centerX, centerY + halfDistance)
            path.lineTo(centerX, centerY - halfDistance)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 120))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture completed: ${if (isUp) "up" else "down"}")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture cancelled: ${if (isUp) "up" else "down"}")
            }
        }, null)
    }

    private fun updateOverlayAppearance() {
        val view = overlayView ?: return

        view.alpha = SettingsStore.buttonOpacity

        val sizeDp = SettingsStore.getButtonSizeDp()
        val sizePx = dpToPx(sizeDp)
        val container = view as? LinearLayout ?: return

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val lp = child.layoutParams as? LinearLayout.LayoutParams ?: continue
            lp.width = sizePx
            lp.height = sizePx
            child.layoutParams = lp
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
                Log.d(TAG, "Overlay removed")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay", e)
            }
        }
        overlayView = null
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
