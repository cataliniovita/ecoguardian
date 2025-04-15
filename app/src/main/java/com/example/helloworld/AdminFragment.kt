package com.example.helloworld

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloworld.databinding.FragmentAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var reportsAdapter: ReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        val view = binding.root
        
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        checkAdminStatus()
        setupClickListeners()
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        reportsAdapter = ReportsAdapter { report ->
            // Handle report click - show details and validation options
            showReportDetails(report)
        }
        
        binding.reportsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = reportsAdapter
        }

        // Load reports from Firestore
        firestore.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error loading reports: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Report::class.java)?.apply {
                            id = doc.id
                        }
                    }
                    reportsAdapter.submitList(reports)
                    
                    // Show empty state if no reports
                    if (reports.isEmpty()) {
                        binding.emptyStateText.visibility = View.VISIBLE
                        binding.reportsRecyclerView.visibility = View.GONE
                    } else {
                        binding.emptyStateText.visibility = View.GONE
                        binding.reportsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun showReportDetails(report: Report) {
        // Show report details dialog with validation options
        val dialog = ReportDetailsDialog(report) { isValidated ->
            if (isValidated) {
                // Update report status to validated
                firestore.collection("reports")
                    .document(report.id)
                    .update("status", "validated")
                    .addOnSuccessListener {
                        Toast.makeText(context, "Report validated successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error validating report: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        dialog.show(childFragmentManager, "ReportDetailsDialog")
    }

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val isAdmin = document.getBoolean("isAdmin") ?: false
                        if (!isAdmin) {
                            Toast.makeText(context, "You don't have admin privileges", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HomeFragment())
                                .commit()
                        }
                    }
                }
        }
    }
    
    private fun setupClickListeners() {
        binding.manageUsersButton.setOnClickListener {
            Toast.makeText(context, "Manage Users functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.manageReportsButton.setOnClickListener {
            Toast.makeText(context, "Manage Reports functionality coming soon", Toast.LENGTH_SHORT).show()
        }
        
        binding.manageSettingsButton.setOnClickListener {
            Toast.makeText(context, "Manage Settings functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 