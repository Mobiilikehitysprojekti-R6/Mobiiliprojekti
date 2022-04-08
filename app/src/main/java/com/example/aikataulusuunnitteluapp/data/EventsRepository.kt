package com.example.aikataulusuunnitteluapp.data

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANResponse
import com.example.aikataulusuunnitteluapp.data.model.ApiBlockedTime
import com.example.aikataulusuunnitteluapp.data.model.ApiEvent
import com.example.aikataulusuunnitteluapp.data.model.ApiResult
import com.example.aikataulusuunnitteluapp.data.model.CalendarEntity
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.time.YearMonth
import android.content.SharedPreferences

class
EventsRepository(private val context: Context) {

    private val eventResponseType = object : TypeToken<List<ApiEvent>>() {}.type
    private val blockedTimeResponseType = object : TypeToken<List<ApiBlockedTime>>() {}.type
    private val gson = Gson()


    fun fetch(
        yearMonths: List<YearMonth>,
        onSuccess: (List<CalendarEntity>) -> Unit
    ) {
        val handlerThread = HandlerThread("events-fetching")
        handlerThread.start()

        val backgroundHandler = Handler(handlerThread.looper)
        val mainHandler = Handler(Looper.getMainLooper())

        backgroundHandler.post {
            val apiEntities = fetchEvents() + fetchBlockedTimes()

            val calendarEntities = yearMonths.flatMap { yearMonth ->
                apiEntities.mapIndexedNotNull { index, apiResult ->
                    apiResult.toCalendarEntity(yearMonth, index)
                }
            }

            mainHandler.post {
                onSuccess(calendarEntities)
            }
        }
    }

    private fun fetchEvents(): List<ApiResult> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("myID", Context.MODE_PRIVATE)
        val userId: String = sharedPreferences.getString("idUser","").toString()
        val request = AndroidNetworking.get("$SERVER_URL/tasks/${userId}").build()
        val response = request.executeForString()
        if (!response.isSuccess) {
            return emptyList()
        }
        return gson.fromJson(response.result.toString(), eventResponseType)

    }

    private fun fetchBlockedTimes(): List<ApiResult> {
        val inputStream = context.assets.open("blocked_times.json")
        val json = inputStream.reader().readText()
        return gson.fromJson(json, blockedTimeResponseType)
    }
}
