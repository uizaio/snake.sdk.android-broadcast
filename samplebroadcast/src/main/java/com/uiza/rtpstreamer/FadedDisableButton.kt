package com.uiza.rtpstreamer

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class FadedDisableButton : AppCompatButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setEnabled(enabled: Boolean) {
        alpha = when {
            enabled -> 1.0f
            else -> 0.2f
        }
        super.setEnabled(enabled)
    }
}
