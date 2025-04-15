package com.example.helloworld

data class Report(
    var id: String = "",
    val userId: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0,
    val status: String = "pending" // pending, validated, rejected
) 