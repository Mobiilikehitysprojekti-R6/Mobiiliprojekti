package com.example.aikataulusuunnitteluapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.dolatkia.animatedThemeManager.ThemeManager
import com.example.aikataulusuunnitteluapp.databinding.ActivityAboutUsBinding
import com.example.aikataulusuunnitteluapp.databinding.ActivityCalendarBinding
import com.example.aikataulusuunnitteluapp.themes.LightTheme
import com.example.aikataulusuunnitteluapp.themes.MyAppTheme
import com.example.aikataulusuunnitteluapp.themes.NightTheme

 class AboutUs : ThemeActivity() {

    lateinit var themePreferences: SharedPreferences
    lateinit var theme: String
    lateinit var userId: String
    private lateinit var binder: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAboutUsBinding.inflate(LayoutInflater.from(this))
        setContentView(binder.root)
    }

    override fun syncTheme(appTheme: AppTheme) {
        // change ui colors with new appThem here
        val myAppTheme = appTheme as MyAppTheme
        binder.root.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the background of the arrow icon
        binder.backOutFromAboutUs.setBackgroundColor(myAppTheme.activityBackgroundColor(this))
        //change the color of the arrow
        binder.backOutFromAboutUs.setColorFilter(myAppTheme.activityIconColor(this))
        binder.tvCredits.setTextColor(myAppTheme.activityTextColor(this))
    }

     override fun getStartTheme(): AppTheme {
         themePreferences = getSharedPreferences("mySettings", Context.MODE_PRIVATE)
         val startTheme = themePreferences.getString("userTheme","").toString()
         println("This is the theme2 in getstarttheme $startTheme")

         //change the theme to match users theme

         if(startTheme.isNotEmpty()) {
             when {
                 startTheme.contains("Night") -> {
                     return NightTheme()
                 }
                 startTheme.contains("Light") -> {
                     return LightTheme()
                 }
             }
         }
         return LightTheme()
     }


    fun closeAboutUsPage(view: View) {
        finish()
    }


 }