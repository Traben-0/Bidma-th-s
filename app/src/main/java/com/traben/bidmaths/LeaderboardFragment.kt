package com.traben.bidmaths

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.traben.bidmaths.databinding.FragmentLeaderboardBinding
import com.traben.bidmaths.databinding.ItemLeaderboardEntryBinding
import com.traben.bidmaths.leaderboard.LeaderBoard
import com.traben.bidmaths.leaderboard.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            val adapter = getRecyclerViewAdapter()
            withContext(Dispatchers.Main) {
                applyAdapter(adapter)
            }
        }

    }

    private suspend fun getRecyclerViewAdapter(): LeaderboardAdapter {
        val database =
            Room.databaseBuilder(requireContext(), LeaderBoard::class.java, "leader-board")
                .build()
        val list = database.getDao().getAllDataScoreOrdered()

        val adapter = LeaderboardAdapter(
            list.ifEmpty {
                listOf(
                    LeaderboardEntry(
                        "No entries yet :)\nplay a game and save your score",
                        -1,
                        ""
                    )
                )
            }
        )
        database.close()
        return adapter
    }

    private fun applyAdapter(adapter: LeaderboardAdapter) {
        this.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class LeaderboardAdapter(private val leaderboardEntries: List<LeaderboardEntry>) :
        RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

        inner class ViewHolder(private val binding: ItemLeaderboardEntryBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(entry: LeaderboardEntry) {
                val animation = AnimationUtils.loadAnimation(context, R.anim.pulse_wobble)
                val randomDuration = 400 + (5..15).random() * (100 - entry.score)
                animation?.duration = randomDuration.toLong()
                binding.score.startAnimation(animation)

                binding.name.text = entry.name
                if (entry.score == -1) {
                    binding.score.text = ""
                    binding.detailsButton.text = ""
                    binding.detailsText.text =
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
                } else {
                    binding.score.text = MathGame.scoreToGrade(entry.score)
                    binding.detailsButton.text = "< more details >"
                    binding.detailsText.text = entry.details
                    var detailBoolean = false
                    binding.detailsText.isVisible = false
                    binding.detailsButton.setOnClickListener {
                        //simple toggle for detailed view
                        detailBoolean = !detailBoolean
                        binding.detailsText.isVisible = detailBoolean
                    }
                }
            }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemLeaderboardEntryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = leaderboardEntries[position]
            holder.bind(entry)
        }

        override fun getItemCount(): Int {
            return leaderboardEntries.size
        }
    }


}
