package com.traben.bidmaths.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.traben.bidmaths.ActivityFragmentArgs
import com.traben.bidmaths.ActivityFragmentDirections
import com.traben.bidmaths.MathGame
import com.traben.bidmaths.R
import com.traben.bidmaths.databinding.FragmentActivityBinding
import com.traben.bidmaths.math.ParsedExpression


/**
 * contains the main game loop
 * functionally this fragment handles a single ParsedMathEquation and its solution activity
 */
class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val args: ActivityFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentActivityBinding.inflate(inflater, container, false)



        if (MathGame.currentMathGame != null) {

            //easier than data binding :/
            MathGame.currentMathGame?.hintSetter = {
                binding.hintText.text = it
            }

            // give hint option functionality
            if (!SettingsFragment.hintsEnabled) {
                binding.hintText.isVisible = false
            }

            //game is still in progress
            equation = MathGame.currentMathGame!!.getEquation(args.gameIteration)


            equation.completeAction = {
                //this lambda is run when the equation is completed
                binding.nextButton.isEnabled = true
                binding.nextButton.text =
                    if (MathGame.currentMathGame!!.isLastGame(args.gameIteration)) "Finish" else "Next"
                if (MathGame.currentMathGame!!.isLastGame(args.gameIteration)) {
                    binding.nextButton.setOnClickListener {
                        findNavController().navigate(ActivityFragmentDirections.actionFinishGame())
                    }
                }
            }

            //build our equation as a nest of binary like views
            binding.content.removeAllViews()
            binding.content.addView(context?.let { equation.getAsView(it) })

            //disable the continue button
            binding.nextButton.isEnabled = false

            val gameLength : String = MathGame.currentMathGame!!.equations.size.toString()
            val gameIteration : String = (args.gameIteration + 1).toString()
            binding.nextButton.text = getString(R.string.next_button_disabled_string,gameIteration,gameLength)
               // "Game ${args.gameIteration + 1} / ${MathGame.currentMathGame!!.equations.size}"

        } else {
            //error
            findNavController().navigate(ActivityFragmentDirections.actionReturnToLanding())
        }
        //this hides the options menu
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
        return binding.root

    }

    //this hides the options menu
    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        @Suppress("DEPRECATION")
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private lateinit var equation: ParsedExpression

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            val action = ActivityFragmentDirections.actionLoopGame(
                gameIteration = args.gameIteration + 1
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}