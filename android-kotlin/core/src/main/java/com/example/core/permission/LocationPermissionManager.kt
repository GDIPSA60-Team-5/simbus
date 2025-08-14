package com.example.core.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class LocationPermissionManager(
    private val activity: Activity? = null,
    private val fragment: Fragment? = null
) {
    
    private val context: Context = activity ?: fragment?.requireContext() 
        ?: throw IllegalArgumentException("Either activity or fragment must be provided")
    
    companion object {
        private val REQUIRED_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        private val BACKGROUND_LOCATION_PERMISSION = arrayOf(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        
        private val NOTIFICATION_PERMISSION = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
    
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private var onBackgroundPermissionResult: ((Boolean) -> Unit)? = null
    private var onNotificationPermissionResult: ((Boolean) -> Unit)? = null
    
    /**
     * Check if basic location permissions are granted
     */
    fun hasLocationPermissions(): Boolean {
        return REQUIRED_LOCATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if background location permission is granted
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Check if notification permission is granted
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
    
    /**
     * Request basic location permissions
     */
    fun requestLocationPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        onResult: (Boolean) -> Unit
    ) {
        onPermissionResult = onResult
        
        if (hasLocationPermissions()) {
            onResult(true)
            return
        }
        
        // Check if we should show rationale
        val shouldShowRationale = REQUIRED_LOCATION_PERMISSIONS.any { permission ->
            activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission) 
            } ?: fragment?.shouldShowRequestPermissionRationale(permission) ?: false
        }
        
        if (shouldShowRationale) {
            showLocationPermissionRationale { 
                launcher.launch(REQUIRED_LOCATION_PERMISSIONS)
            }
        } else {
            launcher.launch(REQUIRED_LOCATION_PERMISSIONS)
        }
    }
    
    /**
     * Request background location permission (should be called after basic permissions are granted)
     */
    fun requestBackgroundLocationPermission(
        launcher: ActivityResultLauncher<Array<String>>,
        onResult: (Boolean) -> Unit
    ) {
        onBackgroundPermissionResult = onResult
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            onResult(true)
            return
        }
        
        if (hasBackgroundLocationPermission()) {
            onResult(true)
            return
        }
        
        if (!hasLocationPermissions()) {
            onResult(false) // Basic permissions required first
            return
        }
        
        showBackgroundLocationPermissionRationale {
            launcher.launch(BACKGROUND_LOCATION_PERMISSION)
        }
    }
    
    /**
     * Request notification permission
     */
    fun requestNotificationPermission(
        launcher: ActivityResultLauncher<Array<String>>,
        onResult: (Boolean) -> Unit
    ) {
        onNotificationPermissionResult = onResult
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onResult(true)
            return
        }
        
        if (hasNotificationPermission()) {
            onResult(true)
            return
        }
        
        launcher.launch(NOTIFICATION_PERMISSION)
    }
    
    /**
     * Handle permission results
     */
    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        when {
            permissions.keys.containsAll(REQUIRED_LOCATION_PERMISSIONS.toList()) -> {
                val allGranted = REQUIRED_LOCATION_PERMISSIONS.all {
                    permissions[it] == true 
                }
                onPermissionResult?.invoke(allGranted)
                
                if (!allGranted) {
                    handleDeniedLocationPermissions()
                }
            }
            
            permissions.containsKey(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                val granted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
                onBackgroundPermissionResult?.invoke(granted)
                
                if (!granted) {
                    showBackgroundLocationDeniedDialog()
                }
            }
            
            permissions.containsKey(Manifest.permission.POST_NOTIFICATIONS) -> {
                val granted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
                onNotificationPermissionResult?.invoke(granted)
            }
        }
    }
    
    private fun showLocationPermissionRationale(onAccept: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location access to provide navigation and bus arrival information. Please grant location permission to continue.")
            .setPositiveButton("Grant Permission") { _, _ -> onAccept() }
            .setNegativeButton("Cancel") { dialog, _ -> 
                dialog.dismiss()
                onPermissionResult?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showBackgroundLocationPermissionRationale(onAccept: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Background Location Permission")
            .setMessage("To receive trip notifications and updates while the app is in the background, please grant 'Allow all the time' location permission in the next screen.")
            .setPositiveButton("Continue") { _, _ -> onAccept() }
            .setNegativeButton("Skip") { dialog, _ -> 
                dialog.dismiss()
                onBackgroundPermissionResult?.invoke(false)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun handleDeniedLocationPermissions() {
        val permanentlyDenied = REQUIRED_LOCATION_PERMISSIONS.any { permission ->
            activity?.let { 
                !ActivityCompat.shouldShowRequestPermissionRationale(it, permission) 
            } ?: fragment?.let {
                !it.shouldShowRequestPermissionRationale(permission)
            } ?: false
        }
        
        if (permanentlyDenied) {
            showPermissionSettingsDialog()
        }
    }
    
    private fun showBackgroundLocationDeniedDialog() {
        AlertDialog.Builder(context)
            .setTitle("Background Location")
            .setMessage("Background location permission was not granted. You can enable it later in app settings if you want to receive notifications while the app is in the background.")
            .setPositiveButton("Open Settings") { _, _ -> openAppSettings() }
            .setNegativeButton("Continue") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(context)
            .setTitle("Location Permission Required")
            .setMessage("Location permissions are essential for this app. Please enable them in app settings.")
            .setPositiveButton("Open Settings") { _, _ -> openAppSettings() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}