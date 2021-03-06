package com.arduia.expense.ui.expense

import com.arduia.expense.ui.expense.swipe.SwipeItemState
import com.arduia.expense.ui.vto.ExpenseVto

sealed class ExpenseLogVo {

    class Header(val date: String) : ExpenseLogVo()

    class Log(
        val expenseLog: ExpenseVto,
        val headerPosition: Int
    ) : ExpenseLogVo()

}