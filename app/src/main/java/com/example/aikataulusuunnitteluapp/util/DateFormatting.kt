package com.example.aikataulusuunnitteluapp.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.MEDIUM
import java.time.format.FormatStyle.SHORT


val defaultDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT)
