package com.arduia.expense.ui.mapping

import com.arduia.core.arch.Mapper
import com.arduia.expense.data.local.ExpenseEnt
import com.arduia.expense.ui.expense.ExpenseLogVo
import javax.inject.Inject

class ExpenseEntToLogVoMapper @Inject constructor(private val mapper: Mapper<ExpenseEnt, ExpenseLogVo.Log>) :
    Mapper<ExpenseEnt, ExpenseLogVo> {
    override fun map(input: ExpenseEnt): ExpenseLogVo {
        return mapper.map(input)
    }
}