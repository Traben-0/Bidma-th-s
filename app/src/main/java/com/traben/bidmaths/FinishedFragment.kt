package com.traben.bidmaths

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.traben.bidmaths.databinding.FragmentFinishedBinding
import com.traben.bidmaths.leaderboard.LeaderBoard
import com.traben.bidmaths.leaderboard.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FinishedFragment : Fragment() {

    private var _binding: FragmentFinishedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFinishedBinding.inflate(inflater, container, false)


        val score = MathGame.currentMathGame?.gameScore()

        if((score ?: 0) <= 0){
            binding.medalIcon.setImageResource(R.drawable.baseline_thumb_down_24)
            binding.medalIconShadow.setImageResource(R.drawable.baseline_thumb_down_24)
        }

        binding.result.text = "score: "+ score.toString()

        binding.playAgainButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                MathGame.loadNewGameInLastMode()
                withContext(Dispatchers.Main) {
                    val action = LandingFragmentDirections.actionStartGame(gameIteration = 0)
                    findNavController().navigate(action)
                }

            }
        }
        binding.returnToMenuButton.setOnClickListener {
            findNavController().navigate(R.id.action_finishedFragment_to_FirstFragment)
        }
        binding.viewLeaderboardButton.setOnClickListener {
//            MathGame.loadHardGame(SettingsFragment.respectLeftRight)
//            val action =  LandingFragmentDirections.actionStartGame(gameIteration = 0)
//            findNavController().navigate(action)
        }

        binding.submitButton.setOnClickListener{button->
            println("what")
            if(binding.submitText.text.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    addToLeaderboard(binding.submitText.text.toString())
                }
                button.isEnabled=false
                binding.submitText.isEnabled = false
            }else{
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
        val randomDuration = (500..1500).random() // Random duration between 500 and 1500 milliseconds
        animation?.duration = randomDuration.toLong()

        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private suspend fun addToLeaderboard(name: String){
        val database = Room.databaseBuilder(requireContext(), LeaderBoard::class.java, "leader-board")
            .build()

        val dao = database.getDao()

        // Insert data
        val newData = LeaderboardEntry(name,MathGame.currentMathGame?.gameScore()?:0, MathGame.currentMathGame?.gameResultsDetailedInfo()?:"null")
        dao.insertData(newData)

        // Retrieve data
        val allData = dao.getAllData()
        println("Map = $allData")
    }

    private var animation : Animation? = null

    override fun onResume() {
        super.onResume()
        binding.medalIcon.startAnimation(animation)
        binding.medalIconShadow.startAnimation(animation)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        animation=null
    }
}