package com.example.gameoflive.data

data class Cell(
    val x: Int,
    val y: Int,
    val type: CellType,
    val alive: Int,
    val neighbors: Int,
    val aliveTimes: Int,
    val direction: CellDirection = CellDirection.NONE,
    val owner: Pair<Int, Int>? = null
)