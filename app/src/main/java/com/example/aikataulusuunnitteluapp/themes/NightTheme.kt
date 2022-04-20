package com.example.aikataulusuunnitteluapp.themes

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.aikataulusuunnitteluapp.R

class NightTheme : MyAppTheme {

    companion object {
        val ThemeId = 1
    }

    override fun id(): Int {
        return ThemeId
    }

    override fun activityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.sunKissed_black)
    }
    override fun activityContainerColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.palm_green)
    }

    override fun activityImageRes(context: Context): Int {
        return R.drawable.image_night
    }

    override fun activityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.dry_orange)
    }

    override fun activityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.light_grey)
    }
    override fun activityHintColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.palm_green)
    }

    override fun activityThemeButtonColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.black)
    }

}