package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etUsernameRegister)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegister)
        val etPassword2 = findViewById<EditText>(R.id.etPasswordRegisterRepeat)
        val button: Button = findViewById<Button>(R.id.btnRegister)

        button.setOnClickListener {
            if (etPassword.text.toString() == etPassword2.text.toString()) {

                val jsonObject = JSONObject()
                try {
                    jsonObject.put("username", etUsername.text.toString())
                    jsonObject.put("password", etPassword.text.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                } // lähetetään käyttäjätunnus + salasana rekisteröintiä varten JSON-objektina

                AndroidNetworking.post("$SERVER_URL/register")
                    .addJSONObjectBody(jsonObject)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(res: JSONObject?) {
                            println("Got response from API: $res")
                            if (res?.get("message").toString() == "Created") {

                                val toast = Toast.makeText(applicationContext, "Account created," +
                                        " please create starting settings", Toast.LENGTH_SHORT)
                                toast.show()

                                val intent = Intent(this@RegisterActivity,
                                        SettingsActivity::class.java)
                                intent.putExtra("userID", res?.get("userID").toString())
                                startActivity(intent)
                                finish()
                                }

                            if (res.toString() == "Username is already taken") {

                                val builder = AlertDialog.Builder(this@RegisterActivity)
                                builder.setTitle("Username is taken")
                                builder.setMessage("Username has been taken!")
                                builder.setPositiveButton("OK") { dialogInterface, which ->

                                    etUsername.setText("")
                                    etPassword.setText("")
                                }
                                builder.show()
                            }
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onError(error: ANError) {
                            println("Error: ${error.errorBody}")

                            val builder = AlertDialog.Builder(this@RegisterActivity)
                            builder.setTitle("Network error!")
                            if (error.errorBody == null) {
                                builder.setMessage("Unknown error")
                            } else {
                                builder.setMessage(error.errorBody.toString())
                            } // alerttia ei tule jos errorBody on null
                            // errorBody on null esim. jos API-yhteyttä ei saada
                            // siihen tehtiin tarkistus että alert tulee jokatapauxessa

                            builder.setPositiveButton("OK") { dialogInterface, which ->
                                println("Failed API Call, not retried")
                            }

                            builder.setNegativeButton("Retry") { dialogInterface, which ->
                                button.performClick()
                            }

                            builder.show()
                        }
                    })
            } else {
                println("Passwords didn't match")

                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setTitle("Passwords don't match!")
                builder.setMessage("Your passwords don't match, please, try again.")
                builder.setPositiveButton("OK") { dialogInterface, which ->
                    etUsername.setText("")
                    etPassword.setText("")
                    etPassword2.setText("")
                }
                builder.show()
            }
        }
    }
}