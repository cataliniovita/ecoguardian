package com.example.helloworld

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.helloworld.databinding.FragmentReportBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class ReportFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var addPhotoButton: MaterialButton
    private lateinit var submitButton: Button
    private lateinit var imageContainer: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_REQUEST_CODE = 1002
    private val CAMERA_REQUEST_CODE = 1003
    
    private var currentLocation: LatLng? = null
    private val selectedImages = mutableListOf<Bitmap>()

    private lateinit var titleInput: TextInputEditText
    private lateinit var locationInput: TextInputEditText
    private lateinit var categoryInput: AutoCompleteTextView

    private var selectedImageUri: Uri? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(context, "Permission required to select images", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.imagePreview.setImageURI(uri)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        locationTextView = binding.locationTextView
        descriptionEditText = binding.descriptionEditText
        addPhotoButton = binding.addPhotoButton
        submitButton = binding.submitButton
        imageContainer = binding.imageContainer

        titleInput = binding.titleInput
        locationInput = binding.locationInput
        categoryInput = binding.categoryInput

        // Set up map
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set up click listeners
        addPhotoButton.setOnClickListener {
            checkPermissionAndPickImage()
        }

        submitButton.setOnClickListener {
            submitReport()
        }

        setupCategoryDropdown()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, get location
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Got location, update map
                        currentLocation = LatLng(location.latitude, location.longitude)
                        map.clear()
                        map.addMarker(
                            MarkerOptions()
                                .position(currentLocation!!)
                                .title("Your Location")
                        )
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                        
                        // Update location text
                        locationTextView.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                    } else {
                        // Location is null, show error
                        Toast.makeText(
                            requireContext(),
                            "Could not get location. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // Permission is not granted, request it
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 and above - use READ_MEDIA_IMAGES
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openImagePicker()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            // Android 12 and below - use READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openImagePicker()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening image picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf(
            "Air Pollution",
            "Water Pollution",
            "Deforestation",
            "Waste Management",
            "Wildlife Protection",
            "Other"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        categoryInput.setAdapter(adapter)
    }

    private fun submitReport() {
        if (currentLocation == null) {
            Toast.makeText(requireContext(), "Please wait for location to be determined", Toast.LENGTH_SHORT).show()
            return
        }
        
        val title = titleInput.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a title and description", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedImages.isEmpty() && selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please add at least one photo", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Disable submit button to prevent multiple submissions
        submitButton.isEnabled = false
        
        // Get current user
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You must be logged in to submit a report", Toast.LENGTH_SHORT).show()
            submitButton.isEnabled = true
            return
        }
        
        // Upload images to Firebase Storage
        val imageUrls = mutableListOf<String>()
        var uploadedImages = 0
        
        if (selectedImages.isNotEmpty()) {
            for (i in selectedImages.indices) {
                val bitmap = selectedImages[i]
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val data = baos.toByteArray()
                
                val imageRef = storage.reference.child("reports/${currentUser.uid}/${UUID.randomUUID()}.jpg")
                
                imageRef.putBytes(data)
                    .addOnSuccessListener { taskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            imageUrls.add(uri.toString())
                            uploadedImages++
                            
                            // If all images are uploaded, save report to Firestore
                            if (uploadedImages == selectedImages.size) {
                                saveReportToFirestore(currentUser.uid, title, description, imageUrls)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                    }
            }
        }

        if (selectedImageUri != null) {
            val imageRef = storage.reference.child("reports/${currentUser.uid}/${UUID.randomUUID()}.jpg")
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        uploadedImages++
                        
                        // If all images are uploaded, save report to Firestore
                        if (uploadedImages == selectedImages.size) {
                            saveReportToFirestore(currentUser.uid, title, description, imageUrls)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                    submitButton.isEnabled = true
                }
        }
    }
    
    private fun saveReportToFirestore(userId: String, title: String, description: String, imageUrls: List<String>) {
        val report = hashMapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "imageUrls" to imageUrls,
            "latitude" to currentLocation!!.latitude,
            "longitude" to currentLocation!!.longitude,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore.collection("reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show()
                
                // Clear form
                titleInput.text?.clear()
                descriptionEditText.text?.clear()
                imageContainer.removeAllViews()
                selectedImages.clear()
                selectedImageUri = null
                binding.imagePreview.visibility = View.GONE
                
                // Re-enable submit button
                submitButton.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
                submitButton.isEnabled = true
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, get location
                    getCurrentLocation()
                } else {
                    // Permission denied, show message
                    Toast.makeText(
                        requireContext(),
                        "Location permission is required to show your location on the map.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open camera
                    openImagePicker()
                } else {
                    // Permission denied, show message
                    Toast.makeText(
                        requireContext(),
                        "Camera permission is required to take photos.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 