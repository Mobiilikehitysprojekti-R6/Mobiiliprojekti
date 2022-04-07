package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import okhttp3.Credentials
import org.json.JSONException
import org.json.JSONObject


class AddTask : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val submitTaskButton: Button = findViewById(R.id.btn_submitTask)
        val headerSubmitText: EditText = findViewById(R.id.et_addTaskHeader)
        val startTimeEditText: EditText = findViewById(R.id.et_addTaskDate)
        val durationEditText: EditText = findViewById(R.id.et_addTaskDuration)


        submitTaskButton.setOnClickListener {
            println("${headerSubmitText.text.toString()}, ${startTimeEditText.text.toString()}, ${durationEditText.text.toString()}")


            val toast = Toast.makeText(applicationContext, "${headerSubmitText.text.toString()}, ${startTimeEditText.text.toString()}, ${durationEditText.text.toString()}", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP or Gravity.RIGHT, 0, 0)
            toast.show()
        }
    }


    fun closeTaskSetter(view: View) {
        finish()
    }
}