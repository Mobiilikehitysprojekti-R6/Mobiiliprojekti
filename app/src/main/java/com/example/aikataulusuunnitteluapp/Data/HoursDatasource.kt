package com.example.aikataulusuunnitteluapp.Data

import com.example.aikataulusuunnitteluapp.Model.HourModel
import com.example.aikataulusuunnitteluapp.R

class HoursDatasource {

    fun loadHourModel(): (List<HourModel>) {
        return listOf<HourModel>(

            HourModel(R.string.hour1),
            HourModel( R.string.hour2),
            HourModel( R.string.hour3),
            HourModel( R.string.hour4),
            HourModel( R.string.hour5),
            HourModel( R.string.hour6),
            HourModel( R.string.hour7),
            HourModel( R.string.hour8),
            HourModel( R.string.hour9),
            HourModel( R.string.hour10),
            HourModel( R.string.hour11),
            HourModel( R.string.hour12),
            HourModel( R.string.hour13),
            HourModel( R.string.hour14),
            HourModel( R.string.hour15),
            HourModel( R.string.hour16),
            HourModel( R.string.hour17),
            HourModel( R.string.hour18),
            HourModel( R.string.hour19),
            HourModel( R.string.hour20),
            HourModel( R.string.hour21),
            HourModel( R.string.hour22),
            HourModel( R.string.hour23),
            HourModel( R.string.hour24),

            )
    }
}