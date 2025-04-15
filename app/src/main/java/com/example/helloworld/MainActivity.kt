package com.example.helloworld

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var bottomNavigation: BottomNavigationView
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User is not logged in, redirect to login activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Check if user is admin
        isAdmin = currentUser.email?.endsWith("@admin.com") == true

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "EcoGuardian"

        // Set up bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation)
        setupNavigation()
        checkAdminStatus()

        // Show admin option only for admin users
        val adminMenuItem = bottomNavigation.menu.findItem(R.id.navigation_admin)
        adminMenuItem.isVisible = isAdmin

        // Set default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            bottomNavigation.selectedItemId = R.id.navigation_home
        }
    }

    private fun setupNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_map -> {
                    replaceFragment(MapFragment())
                    true
                }
                R.id.navigation_report -> {
                    replaceFragment(ReportFragment())
                    true
                }
                R.id.navigation_admin -> {
                    if (isAdmin) {
                        replaceFragment(AdminFragment())
                        true
                    } else {
                        // Show message that user doesn't have admin access
                        android.widget.Toast.makeText(this, "You don't have admin access", android.widget.Toast.LENGTH_SHORT).show()
                        false
                    }
                }
                R.id.navigation_logout -> {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val isAdmin = document.getBoolean("isAdmin") ?: false
                        if (isAdmin) {
                            // Show admin menu item
                            bottomNavigation.menu.findItem(R.id.navigation_admin)?.isVisible = true
                        } else {
                            // Hide admin menu item
                            bottomNavigation.menu.findItem(R.id.navigation_admin)?.isVisible = false
                        }
                    }
                }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 