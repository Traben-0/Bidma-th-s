package com.traben.bidmaths.maths.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.traben.bidmaths.databinding.MathBinaryExpressionBinding
import com.traben.bidmaths.databinding.MathNumberBinding
import com.traben.bidmaths.maths.MathNumber

class MathNumberView(
        number: MathNumber,
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

        private val binding: MathNumberBinding

        init {
                val inflater = LayoutInflater.from(context)
                binding = MathNumberBinding.inflate(inflater, this, true)
                if(number.hasBrackets)
                        binding.number.text =  "($number)"
                else
                        binding.number.text =  number.toString()
        }


}
