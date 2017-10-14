package com.chakmidlot.usefulface.periodic

import android.app.job.JobParameters
import android.app.job.JobService
import com.chakmidlot.usefulface.Data
import org.jsoup.Jsoup
import java.util.*


class WeatherService: JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        UrlLoader(params, this).start()
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }
}

class UrlLoader(val params: JobParameters, val service: JobService): Thread(){

    override fun run() {
        Data.save(service, "/balance/weather", get_forecast())
        service.jobFinished(params, true)
    }
}

private val weatherIcons = mapOf(
        "пасмурно" to "⛅",
        "пасмурно, кратковременный дождь" to "\uD83C\uDF27",
        "ясно" to "\uD83C\uDF23",
        "малооблачно" to "\uD83C\uDF24",
        "облачно" to "⛅"
)

fun get_forecast(): String {
    val calendar = Calendar.getInstance()
    val skip = calendar.get(Calendar.HOUR_OF_DAY) / 6 + 1
    val url = "http://meteo.by/minsk"
    val doc = Jsoup.connect(url).get()

    val current_temp = doc.select(".weather .t strong").text().split(" ")[0]
    val current_sky = doc.select(".weather .s").text()

    val weather_list = doc.select(".b-weather li")
            .flatMap { it.select(".time") }
            .slice(skip .. skip + 1)
            .mapIndexed { index, element ->
                "%s%+d".format(
                        weatherIcons.getOrDefault(
                                element.select(".icon").text(),
                                "--"),
                        element.select(".temp nobr").text()
                                .split(" ")
                                .map{ it.toInt() }
                                .average().toInt())
            }

    return (listOf("${weatherIcons.getOrDefault(current_sky, "--")}$current_temp")
            + weather_list).joinToString("\n")
}
