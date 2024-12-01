package com.example.gameoflive

object CoreLogic {

    fun countAliveNeighbors(cells: MutableList<MutableList<Cell>>, x: Int, y: Int): Int {
        var aliveNeighbors = 0

        for (nx in -1..1) {
            for (ny in -1..1) {
                if (nx == 0 && ny == 0) {
                    continue
                }

                if (nx + x < 0 || nx + x > cells.size - 1
                    || ny + y < 0 || ny + y > cells[y].size - 1
                ) {
                    continue
                }

                if (cells[nx + x][ny + y].alive == 1) {
                    aliveNeighbors++
                }
            }
        }

        return aliveNeighbors
    }

    fun getNeighbors(
        cells: MutableList<MutableList<Cell>>,
        x: Int,
        y: Int,
        onlyFree: Boolean
    ): List<Pair<Int, Int>> {
        val list = mutableListOf<Pair<Int, Int>>()

        for (nx in -1..1) {
            for (ny in -1..1) {
                if (nx == 0 && ny == 0) {
                    continue
                }

                if (nx + x < 0 || nx + x > cells.size - 1
                    || ny + y < 0 || ny + y > cells[y].size - 1
                ) {
                    continue
                }

                if(onlyFree) {
                    if (cells[nx + x][ny + y].alive == 0) {
                        list.add(Pair(nx + x, ny + y))
                    }
                } else {
                    list.add(Pair(nx + x, ny + y))
                }
            }
        }

        return list
    }
}