package com.traben.bidmaths

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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


    val args: ActivityFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentActivityBinding.inflate(inflater, container, false)


        if(MathGame.currentMathGame != null ) {
            if (MathGame.currentMathGame!!.isGameFinished(args.gameIteration)) {
                //game over

                binding.buttonFirst.text = MathGame.currentMathGame!!.gameResults()
                binding.buttonFirst.isEnabled = true
                binding.buttonFirst.setOnClickListener {
                    findNavController().navigate(ActivityFragmentDirections.actionReturnToLanding())
                }
            }else{
                //game is still in progress
                equation = MathGame.currentMathGame!!.getEquation(args.gameIteration)


                equation.completeAction = {
                    //this lambda is run when the equation is completed
                    binding.buttonFirst.isEnabled = true
                    binding.buttonFirst.text = if(MathGame.currentMathGame!!.isLastGame(args.gameIteration)) "Finish" else "Next"
                    if(MathGame.currentMathGame!!.isLastGame(args.gameIteration)){
                        binding.buttonFirst.setOnClickListener {
                            findNavController().navigate(ActivityFragmentDirections.actionFinishGame())
                        }
                    }
                }

                binding.content.removeAllViews()
                binding.content.addView(context?.let { equation.getAsView(it) })

                binding.buttonFirst.isEnabled = false
                binding.buttonFirst.text = "Game ${args.gameIteration+1} / ${MathGame.currentMathGame!!.equations.size}"
            }
        }else{
            //error
            findNavController().navigate(ActivityFragmentDirections.actionReturnToLanding())
        }
        setHasOptionsMenu(true)
        return binding.root

    }



    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    lateinit var equation : ParsedMathEquation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            val action =  ActivityFragmentDirections.actionLoopGame(
                gameIteration = args.gameIteration+1)

            findNavController().navigate(action)
            //findNavController().navigate(R.id.action_SecondFragment_self,

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}