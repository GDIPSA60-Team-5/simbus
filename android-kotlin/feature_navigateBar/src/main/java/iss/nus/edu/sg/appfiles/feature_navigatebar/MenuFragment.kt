package iss.nus.edu.sg.appfiles.feature_navigatebar

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import iss.nus.edu.sg.appfiles.feature_login.ui.LoginActivity
import iss.nus.edu.sg.appfiles.feature_login.util.SecureStorageManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.graphics.toColorInt


class MenuFragment : Fragment() {

    private val PERMISSION_REQUEST_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var tempImageUri: Uri? = null
    private lateinit var profileImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get username from sharePreference
        val username = view.findViewById<TextView>(R.id.username)
        val storageManager = SecureStorageManager(requireContext())
        username.text = storageManager.getUsername()

        // init changePasswordRow
        view.findViewById<LinearLayout>(R.id.changePasswordRow).setOnClickListener {
            startActivity(Intent(requireContext(), ChangePasswordActivity::class.java))
        }

        // init aboutUsRow
        view.findViewById<LinearLayout>(R.id.aboutUsRow).setOnClickListener {
            startActivity(Intent(requireContext(), AboutUsActivity::class.java))
        }

        // init FAQRow
        view.findViewById<LinearLayout>(R.id.faqRow).setOnClickListener {
            startActivity(Intent(requireContext(), FAQActivity::class.java))
        }

        // init logout
        view.findViewById<LinearLayout>(R.id.logoutRow).setOnClickListener {
            handleLogout(storageManager)
        }

        // init profile picture
        profileImage = view.findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        //init mute notification
        val switch1 = view.findViewById<SwitchMaterial>(R.id.switch1)
        handleMuteNotification(switch1)


        // load save avatar
        loadSavedAvatar()
    }

    private fun handleMuteNotification(switch1: SwitchMaterial) {
        switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this@MenuFragment.requireContext(), "mute", Toast.LENGTH_SHORT).show()
                switch1.thumbTintList = ColorStateList.valueOf("#D17A4B".toColorInt())
                // TODO: 开启静音逻辑
            } else {
                Toast.makeText(this@MenuFragment.requireContext(), "not mute", Toast.LENGTH_SHORT).show()
                switch1.thumbTintList = ColorStateList.valueOf("#AA9D96".toColorInt())
                // TODO: 关闭静音逻辑
            }
        }
    }

    private fun handleLogout(storageManager : SecureStorageManager) {
        storageManager.clearAll()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }
    private fun loadSavedAvatar() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val avatarPath = prefs.getString("avatar_path", null)

        if (avatarPath != null && File(avatarPath).exists()) {
            Glide.with(requireContext())
                .load(File(avatarPath))
                .circleCrop()
                .into(profileImage)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Avatar Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery() // open gallery
                    2 -> { /* Cancel - do nothing */ }
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // ask for camera permission
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        tempImageUri = createTempImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        // use ACTION_GET_CONTENT replace ACTION_PICK
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun createTempImageUri(): Uri {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "temp_avatar_${System.currentTimeMillis()}",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                when (permissions[0]) {
                    Manifest.permission.CAMERA -> openCamera()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    tempImageUri?.let {
                        loadImageToView(it)
                        saveAvatarToLocal(it)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let {
                        loadImageToView(it)
                        saveAvatarToLocal(it)
                    }
                }
            }
        }
    }

    private fun loadImageToView(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .circleCrop()
            .into(profileImage)
    }

    private fun saveAvatarToLocal(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "user_avatar.jpg"
            )

            FileOutputStream(file).use { output ->
                inputStream?.copyTo(output)
            }

            // save url into sharePreference
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("avatar_path", file.absolutePath).apply()

            Toast.makeText(
                requireContext(),
                "Avatar saved successfully",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: IOException) {
            Log.e("Avatar", "Failed to save avatar", e)
            Toast.makeText(
                requireContext(),
                "Failed to save avatar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}