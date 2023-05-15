package com.traben.bidmaths.maths.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.traben.bidmaths.databinding.MathBinaryExpressionBinding
import com.traben.bidmaths.databinding.MathNumberBinding

class MathBracketView (
        isLeft: Boolean,
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

        private val binding: MathNumberBinding

        init {
                val inflater = LayoutInflater.from(context)
                binding = MathNumberBinding.inflate(inflater, this, true)
                binding.number.text = if(isLeft) "(" else ")"
        }

}
