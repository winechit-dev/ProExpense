package com.arduia.expense.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.arduia.expense.MainHost
import com.arduia.expense.ui.NavigationDrawer
import com.arduia.expense.R
import com.arduia.expense.databinding.FragExpenseBinding
import com.arduia.expense.ui.common.EventObserver
import com.arduia.expense.ui.common.MarginItemDecoration
import com.arduia.expense.ui.common.ExpenseDetailDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class ExpenseFragment : Fragment(){

    private lateinit var viewBinding: FragExpenseBinding

    private val expenseListAdapter by lazy {
        ExpenseListAdapter( requireContext() )
    }

    private val viewModel by viewModels<ExpenseViewModel>()

    private val itemSwipeCallback  by lazy { ItemSwipeCallback() }

    private val mainHost by lazy {
        requireActivity() as MainHost
    }

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        viewBinding =  FragExpenseBinding.inflate(layoutInflater, null, false).apply {
            initSetupView()
        }
        return viewBinding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lockNavigation()
        setupView()
        setupViewModel()
    }

    private fun createViewBinding() =
        FragExpenseBinding.inflate(layoutInflater, null, false).apply {
            initSetupView()
        }

    private fun FragExpenseBinding.initSetupView(){
         rvExpense.adapter = expenseListAdapter
         rvExpense.layoutManager = LinearLayoutManager(requireContext())
         rvExpense.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_between_items).toInt(),
                resources.getDimension(R.dimen.margin_list_item).toInt()
            ))
    }

    @ExperimentalCoroutinesApi
    private fun setupView(){

        //Setup Transaction Recycler View

        ItemTouchHelper(itemSwipeCallback).attachToRecyclerView(viewBinding.rvExpense)

        itemSwipeCallback.setSwipeListener {
            val item = expenseListAdapter.getItemFromPosition(it)
            viewModel.onItemSelect(item)
        }

        expenseListAdapter.setOnItemClickListener {
            viewModel.selectItemForDetail(it)
        }

        //Close the page
        viewBinding.btnPopBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewBinding.btnCancelDelete.setOnClickListener {
            viewModel.cancelDelete()
        }

        viewBinding.btnDoneDelete.setOnClickListener {
            viewModel.deleteConfirm()
        }
    }

    @ExperimentalCoroutinesApi
    private fun setupViewModel(){

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            when(it){
                true -> viewBinding.pbLoading.visibility = View.VISIBLE
                false -> viewBinding.pbLoading.visibility = View.GONE
            }
        })

        //Expense item Selected, show Confirm
        viewModel.isSelectedMode.observe(viewLifecycleOwner, Observer {

            when(it){
                //Show Confirm dialog after an item is selected
                true -> viewBinding.flConfirmDelete.visibility = View.VISIBLE

                //Hide when none item is selected
                false ->{
                    viewBinding.flConfirmDelete.visibility = View.GONE
                }
            }
        })

        viewModel.detailDataChanged.observe(viewLifecycleOwner, EventObserver {
            ExpenseDetailDialog().apply {
                setEditClickListener {expense ->
                    val action = ExpenseFragmentDirections
                        .actionDestExpenseToDestExpenseEntry(expenseId = expense.id)
                    findNavController().navigate(action,createEntryNavOptions())
                }
            }.showDetail(parentFragmentManager,it)
        })

        viewModel.deleteEvent.observe( viewLifecycleOwner, EventObserver {
            val message = when {
                it > 0 -> "$it ${getString(R.string.label_single_item_deleted)}"
                else -> "$it ${getString(R.string.label_multi_item_deleted)}"
            }
            mainHost.showSnackMessage(message)
        })
    }

    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MainScope().launch(Dispatchers.Main) {
            val animationDuration = resources.getInteger(R.integer.expense_anim_left_duration)
            delay(animationDuration.toLong())
            viewModel.getExpenseLiveData().observe(viewLifecycleOwner, Observer {
                expenseListAdapter.submitList(it)
            })
        }
    }

    private fun lockNavigation(){
        //Lock Navigation Drawer Left
        (requireActivity() as? NavigationDrawer)?.lockDrawer(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        //Unlock the Navigation Drawer Left
        (requireActivity() as? NavigationDrawer)?.lockDrawer(false)
    }

    private fun createEntryNavOptions() =
        NavOptions.Builder()
            //For Entry Fragment
            .setEnterAnim(R.anim.pop_down_up)
            .setPopExitAnim(R.anim.pop_up_down)
            //For Home Fragment
            .setExitAnim(android.R.anim.fade_out)
            .setPopEnterAnim(R.anim.nav_default_enter_anim)

            .build()
}
