package com.example.gameoflive

data class Cell(
    val x: Int,
    val y: Int,
    val alive: Int,
    val neighbors: Int,
    val aliveTimes: Int
)