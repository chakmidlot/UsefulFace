package com.chakmidlot.usefulface

object SmsParser {

    private val BELINVEST_CARD = "0826"

    private val PRIOR_CARD = "7694"
    private val PRIOR_INTERNET_CARD = "1303"

    fun parse(bank: String, message: String): Pair<String, String> {
        return if (bank == "Belinvest") {
            belinvest(message)
        }
        else if (bank == "Priorbank") {
            prior(message)
        }
        else {
            Pair("", "")
        }
    }

    fun belinvest(body: String): Pair<String, String> {
        val lines = body.split("\n")
        return if (lines[0] == "BANK *${BELINVEST_CARD} ") {
            val balanceLine = lines.filter { it.startsWith("OST=") }
            val balance = balanceLine[0].substring(4, balanceLine[0].length - 7)
            Pair("belinvest_main", balance)
        }
        else {
            Pair("", "")
        }
    }

    fun prior(body: String): Pair<String, String> {
        val regex = Regex("Priorbank.*\\d\\*{3}(\\d{4}).*Dostupno:(.*?)\\.")
        val match = regex.find(body)
        if (match != null) {
            val balance = match.groups[2]!!.value
            val card = match.groups[1]?.value

            return if (card == PRIOR_CARD) {
                val balance = (match.groups[2]!!.value
                        .replace(" ", "")
                        .replace(",", "")
                        .toInt() - 1500).toString()
                Pair("prior_main", balance)
            }
            else if (card == PRIOR_INTERNET_CARD) {
                Pair("prior_internet", match.groups[2]!!.value)
            }
            else {
                Pair("", "")
            }
        }
        return Pair("", "")
    }
}
