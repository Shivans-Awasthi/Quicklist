package com.example.quicklist

import java.util.Date

data class Todo(
    val title: String,
    val tasktime:Long,
    val taskdetail: String,
    val notifid: Int,
    val category: String,
    var isChecked: Boolean = false

)

