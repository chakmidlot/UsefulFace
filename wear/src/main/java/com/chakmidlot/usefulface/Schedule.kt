package com.chakmidlot.usefulface


val data = listOf(
    Pair("84", "0:08"),
    Pair("84", "6:01"),
    Pair("84", "6:12"),
    Pair("84", "6:21"),
    Pair("84", "6:30"),
    Pair("84", "6:39"),
    Pair("84", "6:48"),
    Pair("84", "6:56"),
    Pair("84", "7:03"),
    Pair("84", "7:10"),
    Pair("84", "7:19"),
    Pair("84", "7:26"),
    Pair("84", "7:35"),
    Pair("84", "7:44"),
    Pair("84", "7:52"),
    Pair("84", "8:00"),
    Pair("84", "8:08"),
    Pair("84", "8:18"),
    Pair("84", "8:28"),
    Pair("84", "8:38"),
    Pair("84", "8:48"),
    Pair("84", "8:58"),
    Pair("84", "9:08"),
    Pair("84", "9:18"),
    Pair("84", "9:27"),
    Pair("84", "9:39"),
    Pair("84", "9:51"),
    Pair("84", "10:04"),
    Pair("84", "10:17"),
    Pair("84", "10:31"),
    Pair("84", "10:45"),
    Pair("84", "10:59"),
    Pair("84", "11:13"),
    Pair("84", "11:27"),
    Pair("84", "11:41"),
    Pair("84", "11:55"),
    Pair("84", "12:09"),
    Pair("84", "12:23"),
    Pair("84", "12:37"),
    Pair("84", "12:51"),
    Pair("84", "13:05"),
    Pair("84", "13:19"),
    Pair("84", "13:33"),
    Pair("84", "13:47"),
    Pair("84", "14:00"),
    Pair("84", "14:13"),
    Pair("84", "14:26"),
    Pair("84", "14:39"),
    Pair("84", "14:52"),
    Pair("84", "15:04"),
    Pair("84", "15:16"),
    Pair("84", "15:29"),
    Pair("84", "15:41"),
    Pair("84", "15:53"),
    Pair("84", "16:04"),
    Pair("84", "16:14"),
    Pair("84", "16:24"),
    Pair("84", "16:34"),
    Pair("84", "16:44"),
    Pair("84", "16:54"),
    Pair("84", "17:04"),
    Pair("84", "17:14"),
    Pair("84", "17:24"),
    Pair("84", "17:34"),
    Pair("84", "17:44"),
    Pair("84", "17:54"),
    Pair("84", "18:05"),
    Pair("84", "18:17"),
    Pair("84", "18:29"),
    Pair("84", "18:41"),
    Pair("84", "18:53"),
    Pair("84", "19:05"),
    Pair("84", "19:18"),
    Pair("84", "19:33"),
    Pair("84", "19:49"),
    Pair("84", "20:05"),
    Pair("84", "20:21"),
    Pair("84", "20:36"),
    Pair("84", "20:52"),
    Pair("84", "21:06"),
    Pair("84", "21:20"),
    Pair("84", "21:34"),
    Pair("84", "21:47"),
    Pair("84", "22:01"),
    Pair("84", "22:15"),
    Pair("84", "22:28"),
    Pair("84", "22:41"),
    Pair("84", "22:54"),
    Pair("84", "23:07"),
    Pair("84", "23:21"),
    Pair("84", "23:38"),
    Pair("84", "23:53"),
    Pair("84", "24:08"))


data class Bus (val remains: String, val route: String, val time: String)


class Schedule {

    private val preparedData: List<Triple<Int, String, String>>

    init {
        preparedData = data.map{ Triple(parseStringTime(it.second), it.first, it.second)}.sortedBy { it.first }
    }

    fun getNearests(currentTime: Int): List<Bus> {
        for (i in preparedData.indices) {
            if (currentTime <= preparedData[i].first) {
                return preparedData.subList(maxOf(0, i-1), minOf(preparedData.count(), i + 3))
                        .map { Bus("%+d".format(it.first - currentTime).toString(), it.second, it.third) }
            }
        }
        return listOf()
    }

    private fun parseStringTime(time: String): Int {
        val time_ints = time.split(":").map{ it.toInt() }
        return time_ints[0] * 60 + time_ints[1]
    }
}
