package com.traben.bidmaths.math.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.traben.bidmaths.databinding.MathNumberBinding


/**
 * simple view for expression brackets to be constructed quickly with a boolean
 * */
@SuppressLint("ViewConstructor")//not a concern
class MathBracketView(
    isLeft: Boolean,
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: MathNumberBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = MathNumberBinding.inflate(inflater, this, true)
        binding.number.text = if (isLeft) "(" else ")"

        binding.numberShadow.text = binding.number.text
    }

}
