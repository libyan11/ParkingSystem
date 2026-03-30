package com.example.parkingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()


        binding.Buttonlogin.setOnClickListener {
            val email = binding.editTextUserName.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()


            val error = validateInput(email, password)
            if (error != null) {
                binding.textViewMessage.text = error
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()


                        val intent = Intent(this, MenuActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {

                        binding.textViewMessage.text = "Login Failed: ${task.exception?.message}"
                    }
                }
        }


        binding.buttonRegister.setOnClickListener {
            startActivity(Intent(this, MainActivityNewUser::class.java))
        }
    }


    companion object {
        fun validateInput(email: String, password: String): String? {
            return if (email.isEmpty() || password.isEmpty()) {
                "Please enter your email and password."
            } else {
                null
            }
        }
    }
}