package com.example.parkingapp.controller

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parkingapp.R
import com.example.parkingapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewuserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newuser)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    fun saveNewUserButton(view: View) {
        val firstName = findViewById<EditText>(R.id.editTextFirstName).text.toString().trim()
        val lastName = findViewById<EditText>(R.id.editTextLastName).text.toString().trim()
        val age = findViewById<EditText>(R.id.editTextNumberAge).text.toString().trim()
        val address = findViewById<EditText>(R.id.editTextAddress).text.toString().trim()
        val email = findViewById<EditText>(R.id.editTextNewUserName).text.toString().trim()
        val password = findViewById<EditText>(R.id.editTextNewUserPassword).text.toString().trim()

        val messageText = findViewById<TextView>(R.id.textViewMessage)

        if (email.isEmpty() || password.isEmpty()) {
            messageText.text = "Email and password required"
            return
        }

        val user = User(
            firstName = firstName,
            lastName = lastName,
            age = age,
            address = address,
            email = email
        )

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User Registered!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                messageText.text = "Firestore Error: ${e.message}"
                            }
                    }
                } else {
                    messageText.text = "Auth Error: ${task.exception?.message}"
                }
            }
    }

    fun backButton(view: View) {
        finish()
    }
}