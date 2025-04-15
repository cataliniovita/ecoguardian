package com.example.helloworld

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ReportDetailsDialog(
    private val report: Report,
    private val onValidationResult: (Boolean) -> Unit
) : DialogFragment() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Report Details")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_report_details, container, false)
        
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        val imageView = view.findViewById<ImageView>(R.id.reportImage)
        val descriptionText = view.findViewById<TextView>(R.id.descriptionText)
        val locationText = view.findViewById<TextView>(R.id.locationText)
        val validateButton = view.findViewById<Button>(R.id.validateButton)
        val rejectButton = view.findViewById<Button>(R.id.rejectButton)

        // Load image using Glide
        Glide.with(this)
            .load(report.imageUrl)
            .into(imageView)

        // Set text fields
        descriptionText.text = report.description
        locationText.text = report.location

        // Set up buttons
        validateButton.setOnClickListener {
            updateReportStatus("validated")
            onValidationResult(true)
            dismiss()
        }

        rejectButton.setOnClickListener {
            updateReportStatus("rejected")
            onValidationResult(false)
            dismiss()
        }

        return view
    }

    private fun updateReportStatus(status: String) {
        firestore.collection("reports")
            .document(report.id)
            .update("status", status)
    }
} 