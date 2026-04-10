package com.example.parkingapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        val firstName = findViewById<EditText>(R.id.editTextFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.editTextLastName).text.toString()
        val age = findViewById<EditText>(R.id.editTextNumberAge).text.toString()
        val address = findViewById<EditText>(R.id.editTextAddress).text.toString()
        val email = findViewById<EditText>(R.id.editTextNewUserName).text.toString()
        val password = findViewById<EditText>(R.id.editTextNewUserPassword).text.toString()

        val messageText = findViewById<TextView>(R.id.textViewMessage)

        if (email.isEmpty() || password.isEmpty()) {
            messageText.text = "Email and password required"
            return
        }

        //  Step 1: Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid

                    // Step 2: Save extra data in Firestore
                    val userMap = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "age" to age,
                        "address" to address,
                        "email" to email
                    )

                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .set(userMap)
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