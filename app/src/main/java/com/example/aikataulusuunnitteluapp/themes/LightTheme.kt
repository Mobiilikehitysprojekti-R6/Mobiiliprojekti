package com.example.aikataulusuunnitteluapp.themes

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.aikataulusuunnitteluapp.R

class LightTheme : MyAppTheme {

    companion object {
        const val ThemeId = 0
    }

    override fun id(): Int {
        return ThemeId
    }

    override fun activityBackgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.light_grey)
    }
    override fun activityContainerColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.sunKissed_green)
    }

    override fun activityImageRes(context: Context): Int {
        return R.drawable.image_light
    }

    override fun activityIconColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.beach)
    }

    override fun activityTextColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.sunKissed_black)
    }

    override fun activityButtonColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.sunKissed_green)
    }

    override fun activityHintColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.sunKissed_green)
    }

    override fun activityThemeButtonColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.beach)
    }
}
