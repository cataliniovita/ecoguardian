package com.example.helloworld

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class DebugFirebaseActivity : AppCompatActivity() {
    private lateinit var debugTextView: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_firebase)

        debugTextView = findViewById(R.id.debugTextView)
        auth = FirebaseAuth.getInstance()

        val debugInfo = StringBuilder()
        debugInfo.append("Firebase Debug Information:\n\n")

        // Check Firebase initialization
        debugInfo.append("Firebase Initialization:\n")
        debugInfo.append("FirebaseApp initialized: ${FirebaseApp.getInstance() != null}\n\n")

        // Check Authentication state
        debugInfo.append("Authentication State:\n")
        val currentUser = auth.currentUser
        debugInfo.append("Current user: ${currentUser?.email ?: "Not signed in"}\n")
        debugInfo.append("User ID: ${currentUser?.uid ?: "N/A"}\n")
        debugInfo.append("Email verified: ${currentUser?.isEmailVerified ?: false}\n")

        debugTextView.text = debugInfo.toString()
    }
} 