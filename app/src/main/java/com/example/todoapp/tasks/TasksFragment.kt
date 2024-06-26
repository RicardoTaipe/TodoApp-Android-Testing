package com.example.todoapp.tasks

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.EventObserver
import com.example.todoapp.R
import com.example.todoapp.TodoApplication
import com.example.todoapp.databinding.FragmentTasksBinding
import com.example.todoapp.util.setupRefreshLayout
import com.example.todoapp.util.setupSnackbar
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class TasksFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentTasksBinding

    private val viewModel by viewModels<TasksViewModel> {
        TasksViewModelFactory((requireContext().applicationContext as TodoApplication).taskRepository)
    }

    private lateinit var listAdapter: TasksAdapter
    private val args: TasksFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.lifecycleOwner = viewLifecycleOwner
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(binding.refreshLayout, binding.tasksList)
        setupNavigation()
        setUPChipsSelection()
    }

    private fun setUPChipsSelection() {
        val inflater = LayoutInflater.from(binding.filteringSelection.context)
        val children: List<Chip> = TasksFilterType.values().map {
            val chip: Chip =
                inflater.inflate(R.layout.filter, binding.filteringSelection, false) as Chip
            chip.text = it.toString()
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.setFiltering(it)
                }
            }
            chip
        }
        binding.filteringSelection.removeAllViews()

        for (chip in children) {
            binding.filteringSelection.addView(chip)
        }
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT, binding.addTaskFab)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun setupListAdapter() {
        val viewModel = binding.viewmodel
        if (viewModel != null) {
            listAdapter = TasksAdapter(viewModel)
            binding.tasksList.adapter = listAdapter
        } else {
            Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }

    private fun setupNavigation() {
        viewModel.openTaskEvent.observe(viewLifecycleOwner, EventObserver {
            openTaskDetails(it)
        })
        viewModel.newTaskEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToAddNewTask()
        })
    }

    private fun openTaskDetails(taskId: String) {
        val action = TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment(taskId)
        findNavController().navigate(action)
    }

    private fun navigateToAddNewTask() {
        val action = TasksFragmentDirections
            .actionTasksFragmentToAddEditTaskFragment(
                null,
                resources.getString(R.string.add_task)
            )
        findNavController().navigate(action)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedTasks()
                true
            }
            R.id.menu_dark_mode -> {
                openSettings()
                true
            }
            R.id.menu_refresh -> {
                viewModel.loadTasks(true)
                true
            }
            else -> false
        }
    }

    private fun openSettings() {
        val action = TasksFragmentDirections.actionTasksFragmentDestToSettingsFragment()
        findNavController().navigate(action)
    }
}

//https://medium.com/androiddevelopers/migrating-to-material-components-for-android-ec6757795351
//https://material.io/blog/migrating-material-3
//theming https://codelabs.developers.google.com/codelabs/mdc-101-kotlin#2

//POSIBLE THEMING
//https://dribbble.com/shots/14153121-ToDo-App-Dark-Theme
//https://dribbble.com/shots/5540051-To-do-app-concept/attachments/10924003?mode=media

//POSSIBLE FEATURES
//https://search.muz.li/NDQxYjNhNzVh
//https://dribbble.com/shots/4544339-To-do-App-Matt-s-Profile