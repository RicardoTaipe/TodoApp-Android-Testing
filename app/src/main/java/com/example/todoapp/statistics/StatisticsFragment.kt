package com.example.todoapp.statistics

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.todoapp.TodoApplication
import com.example.todoapp.databinding.StatisticsFragmentBinding
import com.example.todoapp.util.setupRefreshLayout

private const val DURATION = 800L
private const val PROGRESS_PROPERTY = "progress"

class StatisticsFragment : Fragment() {

    private lateinit var binding: StatisticsFragmentBinding

    private val viewModel by viewModels<StatisticsViewModel> {
        StatisticsViewModelFactory(
            (requireContext().applicationContext as TodoApplication).taskRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        this.setupRefreshLayout(binding.refreshLayout)
        animateBars()
    }

    private fun animateBars() {
        viewModel.activeTasksPercent.observe(viewLifecycleOwner) {
            ObjectAnimator.ofInt(binding.statsActiveIndicator, PROGRESS_PROPERTY, 0, it.toInt())
                .apply {
                    duration = DURATION
                    start()
                }
        }
        viewModel.completedTasksPercent.observe(viewLifecycleOwner) {
            ObjectAnimator.ofInt(binding.statsCompletedIndicator, PROGRESS_PROPERTY, 0, it.toInt())
                .apply {
                    duration = DURATION
                    start()
                }
        }
    }
}