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

        // init buttons and edittext resources
        val etUsername = findViewById<EditText>(R.id.etUsernameRegister)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegister)
        val etPassword2 = findViewById<EditText>(R.id.etPasswordRegisterRepeat)
        val button: Button = findViewById<Button>(R.id.btnRegister)

        button.setOnClickListener {
            if (etPassword.text.toString() == etPassword2.text.toString()) {

                // if "password" and "repeat password" fields match,
                // we begin to post the new user to API

                val jsonObject = JSONObject()
                try {
                    jsonObject.put("username", etUsername.text.toString())
                    jsonObject.put("password", etPassword.text.toString())
                } catch (e: JSONException) {
                    e.printStackTrace()
                } // send username + password as a JSON-object for registration

                AndroidNetworking.post("$SERVER_URL/register")
                    .addJSONObjectBody(jsonObject)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(res: JSONObject?) {
                            println("Got response from API: $res")
                            if (res?.get("message").toString() == "Created") {

                                // if we get Created code back, registration was successful.
                                // redirect to SettingsActivity

                                val toast = Toast.makeText(applicationContext, "Tili luotu," +
                                        " ole hyvä ja anna alustavat asetukset", Toast.LENGTH_SHORT)
                                toast.show()

                                val intent = Intent(this@RegisterActivity,
                                        SettingsActivity::class.java)
                                intent.putExtra("userID", res?.get("userID").toString())
                                startActivity(intent)
                                finish()
                                }

                            if (res.toString() == "Username is already taken") {

                                val builder = AlertDialog.Builder(this@RegisterActivity)
                                builder.setTitle("Käyttäjänimi on jo otettu")
                                builder.setMessage("Käyttäjänimi on jo otettu, yritä uudelleen.")
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
                            builder.setTitle("Verkkovirhe!")
                            if (error.errorBody == null) {
                                builder.setMessage("Tuntematon virhe")
                            } else {
                                builder.setMessage(error.errorBody.toString())
                            } // no alert if errorBody is NULL
                            // errorBody is NULL if we don't get an API connection
                            // i made a check that you should get an error nevertheless

                            builder.setPositiveButton("OK") { dialogInterface, which ->
                            }

                            builder.setNegativeButton("Retry") { dialogInterface, which ->
                                button.performClick()
                            }

                            builder.show()
                        }
                    })
            } else {

                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setTitle("Salasanat eivät ole samat!")
                builder.setMessage("Salasanasi eivät täsmänneet, yritä uudelleen.")
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