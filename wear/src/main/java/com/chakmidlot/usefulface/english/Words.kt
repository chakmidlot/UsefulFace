package com.chakmidlot.usefulface.english

import java.util.*


class Words {
    private val rand = Random()

    fun next(): Pair<String, String> {
        return vocabulary[rand.nextInt(vocabulary.size)]
    }

}
