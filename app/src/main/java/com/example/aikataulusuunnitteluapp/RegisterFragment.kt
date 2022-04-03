package com.example.aikataulusuunnitteluapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.example.aikataulusuunnitteluapp.data.SERVER_URL
import org.json.JSONException
import org.json.JSONObject


class RegisterFragment : Fragment() {

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.fragment_register,
            container, false
        )

        val etUsername = view.findViewById<EditText>(R.id.etUsernameRegister)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegister)
        val etPassword2 = view.findViewById<EditText>(R.id.etPasswordRegisterRepeat)
        val button: Button = view.findViewById<View>(R.id.btnRegisterFragment) as Button

        button.setOnClickListener {
            if(etPassword.text.toString() == etPassword2.text.toString()) {

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
                    .getAsString(object : StringRequestListener {
                        override fun onResponse(res: String?) {
                            println("Got response from API: $res")
                            if (res.toString() == "Registered") {

                                val builder = AlertDialog.Builder(activity!!)
                                builder.setTitle("Account registered!")
                                builder.setMessage("Your account was successfully registered, please login")
                                builder.setPositiveButton("OK"){dialogInterface, which ->
                                    val intent = Intent(activity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                builder.show()
                            }

                            if (res.toString() == "Username is already taken") {

                                val builder = AlertDialog.Builder(activity!!)
                                builder.setTitle("Username is taken")
                                builder.setMessage("Username has been taken!")
                                builder.setPositiveButton("OK"){dialogInterface, which ->

                                    etUsername.setText("")
                                    etPassword.setText("")
                                }
                                builder.show()
                            }
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onError(error: ANError) {
                            println("Error: ${error.errorBody}")

                            val builder = AlertDialog.Builder(activity!!)
                            builder.setTitle("Network error!")
                            if (error.errorBody == null) {
                                builder.setMessage("Unknown error")
                            } else {
                                builder.setMessage(error.errorBody.toString())
                            } // alerttia ei tule jos errorBody on null
                            // errorBody on null esim. jos API-yhteyttä ei saada
                            // siihen tehtiin tarkistus että alert tulee jokatapauxessa

                            builder.setPositiveButton("OK"){dialogInterface, which ->
                                println("Failed API Call, not retried")
                            }

                            builder.setNegativeButton("Retry"){dialogInterface, which ->
                                button.performClick()
                            }

                            builder.show()
                        }
                    })
            } else {
                println("Passwords didn't match")

                val builder = AlertDialog.Builder(activity!!)
                builder.setTitle("Passwords don't match!")
                builder.setMessage("Your passwords don't match, please, try again.")
                builder.setPositiveButton("OK"){dialogInterface, which ->
                    etUsername.setText("")
                    etPassword.setText("")
                    etPassword2.setText("")
                }
                builder.show()
            }
        }
        return view
    }
}