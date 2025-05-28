package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileEditActivity : BaseCompact() {

    private lateinit var profileImage: ImageView
    private lateinit var btnChangePhoto: Button
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextCurrentPassword: TextInputEditText
    private lateinit var editTextNewPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var btnSaveChanges: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var editBack: ImageView

    private lateinit var auth: FirebaseAuth
    private val storageRef = FirebaseStorage.getInstance().reference
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        auth = Firebase.auth

        profileImage = findViewById(R.id.profileImageEdit)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        editTextName = findViewById(R.id.editTextName)
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword)
        editTextNewPassword = findViewById(R.id.editTextNewPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        progressBar = findViewById(R.id.progressBar)
        editBack = findViewById(R.id.editBack)

        editBack.setOnClickListener {
            bagla(ProfileActivity::class.java, false)

        }


        val currentUser = auth.currentUser
        currentUser?.let {
            editTextName.setText(it.displayName ?: "")
            it.photoUrl?.let { uri ->
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(profileImage)
            }
        }

        btnChangePhoto.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Profil Fotoğrafını Seç"), PICK_IMAGE_REQUEST)
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Picasso.get()
                .load(selectedImageUri)
                .placeholder(R.drawable.baseline_person_24)
                .into(profileImage)
        }
    }

    private fun saveChanges() {
        val currentUser = auth.currentUser ?: return
        val newName = editTextName.text.toString().trim()
        val currentPassword = editTextCurrentPassword.text.toString().trim()
        val newPassword = editTextNewPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()

        progressBar.visibility = View.VISIBLE
        btnSaveChanges.isEnabled = false


        val updates = mutableListOf<() -> Unit>()


        if (selectedImageUri != null) {
            updates.add {
                uploadImageToFirebase(selectedImageUri!!) {
                    updateProfileChain(updates, 1)
                }
            }
        }


        if (newName.isNotEmpty() && newName != currentUser.displayName) {
            updates.add {
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }
                currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateProfileChain(updates, 1)
                        } else {
                            progressBar.visibility = View.GONE
                            btnSaveChanges.isEnabled = true
                            Toast.makeText(this, "İsim güncellenirken hata oluştu: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


        if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (newPassword != confirmPassword) {
                progressBar.visibility = View.GONE
                btnSaveChanges.isEnabled = true
                Toast.makeText(this, "Yeni şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.length < 6) {
                progressBar.visibility = View.GONE
                btnSaveChanges.isEnabled = true
                Toast.makeText(this, "Şifre en az 6 karakter olmalıdır!", Toast.LENGTH_SHORT).show()
                return
            }

            updates.add {
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            currentUser.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        updateProfileChain(updates, 1)
                                    } else {
                                        progressBar.visibility = View.GONE
                                        btnSaveChanges.isEnabled = true
                                        Toast.makeText(this, "Şifre güncellenirken hata oluştu: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            progressBar.visibility = View.GONE
                            btnSaveChanges.isEnabled = true
                            Toast.makeText(this, "Mevcut şifre yanlış!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        if (updates.isEmpty()) {

            progressBar.visibility = View.GONE
            btnSaveChanges.isEnabled = true
            Toast.makeText(this, "Değişiklik yapılmadı", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Start the chain
            updateProfileChain(updates, 0)
        }
    }

    private fun updateProfileChain(updates: List<() -> Unit>, index: Int) {
        if (index < updates.size) {
            updates[index]()
        } else {

            progressBar.visibility = View.GONE
            btnSaveChanges.isEnabled = true
            Toast.makeText(this, "Profil başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, onComplete: () -> Unit) {
        val fileRef = storageRef.child("profil_fotograflari/${auth.currentUser?.uid}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileUpdates = userProfileChangeRequest {
                        photoUri = uri
                    }
                    auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete()
                        } else {
                            progressBar.visibility = View.GONE
                            btnSaveChanges.isEnabled = true
                            Toast.makeText(this, "Profil fotoğrafı güncellenirken hata oluştu!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                btnSaveChanges.isEnabled = true
                Toast.makeText(this, "Fotoğraf yüklenirken hata oluştu!", Toast.LENGTH_SHORT).show()
            }
    }
}