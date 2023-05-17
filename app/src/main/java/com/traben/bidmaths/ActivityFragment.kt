package com.traben.bidmaths

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.traben.bidmaths.databinding.FragmentActivityBinding
import com.traben.bidmaths.maths.ParsedMathEquation

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentActivityBinding.inflate(inflater, container, false)



        equation = ParsedMathEquation.createRandomExpression(3)
        binding.content.removeAllViews()
        binding.content.addView(context?.let { equation.getAsView(it) })

        binding.buttonFirst.text = equation.toStringPretty()

        return binding.root

    }

    lateinit var equation : ParsedMathEquation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}