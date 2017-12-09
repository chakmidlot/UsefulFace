package com.chakmidlot.usefulface.english


import android.util.Log
import java.lang.Math.log
import java.lang.Math.pow
import java.util.*


class Words {
    private val MEMORY_LEVELS_NUMBER = 4
    private val BASE = 1.1



    private val rand = Random()

    private var initial_random = 0
    private var level = 0
    private var word_index = 0

    private val memory_levels: MutableList<MutableList<Pair<String, String>>>
    private val max_value: Double

    init {
        memory_levels = (1..MEMORY_LEVELS_NUMBER)
                .map { emptyList<Pair<String, String>>().toMutableList() }
                .toMutableList()

        vocabulary.forEachIndexed {
            index, word -> memory_levels[index % MEMORY_LEVELS_NUMBER].add(word)
        }

        max_value = pow(BASE, MEMORY_LEVELS_NUMBER.toDouble()) - 1
    }

    fun next(know_previous: Boolean? =null): Pair<String, String> {
        if (know_previous != null) {
            if (know_previous) {
                if (level != 0) {
                    val word = memory_levels[level][word_index]
                    memory_levels[level].removeAt(word_index)
                    level -= 1
                    memory_levels[level].add(word)
                }
            }
            else {
                if (level != MEMORY_LEVELS_NUMBER - 1) {
                    val word = memory_levels[level][word_index]
                    memory_levels[level].removeAt(word_index)
                    level += 1
                    memory_levels[level].add(word)
                }
            }
            val memory_sizes = memory_levels.map {it.count()}.joinToString(", ")
            Log.d("UsefulFace", "Level counts: $memory_sizes")
        }

        val rnd = rand.nextDouble() * max_value + 1
        initial_random = (log(rnd) / log(BASE)).toInt()
        level = initial_random

        while (memory_levels[level].count() == 0) {
            level = (level + MEMORY_LEVELS_NUMBER - 1) % MEMORY_LEVELS_NUMBER
        }

        word_index = rand.nextInt(memory_levels[level].count())
        return memory_levels[level][word_index]
    }

}
