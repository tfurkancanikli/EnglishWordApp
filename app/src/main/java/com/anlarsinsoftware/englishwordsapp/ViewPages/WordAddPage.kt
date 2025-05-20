package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityWordAddPageBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.UUID

class WordAddPage : BaseCompact() {

    private lateinit var binding: ActivityWordAddPageBinding
    private lateinit var galeriLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var gorsel: Uri? = null
    private var bitmap: Bitmap? = null
    private val dataBase = Firebase.firestore
    val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordAddPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()





        galeriLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                gorsel = intent?.data
                if (gorsel != null) {
                    try {
                        bitmap = if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(contentResolver, gorsel!!)
                            ImageDecoder.decodeBitmap(source)
                        } else {
                            MediaStore.Images.Media.getBitmap(contentResolver, gorsel)
                        }
                        binding.gorselEkleBtn.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }


        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galeriLauncher.launch(galeriIntent)
            } else {
                Toast.makeText(this, "İzin verilmedi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun KelimeEkleClick(view: View) {
        if (gorsel != null) {
            binding.kelimeEkleBtn.isEnabled = false // Tek tıklamayı sağlamak için
            binding.progressBar3.visibility = View.VISIBLE

            val ref = storage.reference
            val uid = UUID.randomUUID()
            val imageName = "$uid.jpg"
            val gorselRef = ref.child("gorsel").child(imageName)

            gorselRef.putFile(gorsel!!)
                .addOnSuccessListener {
                    val yuklenen = ref.child("gorsel").child(imageName)
                    yuklenen.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        val kelimeIng = binding.ingilizceKelimeEdit.text.toString()
                        val kelimeTur = binding.turkceKarsilikEdit.text.toString()
                        val cumle1 = binding.cumle1Edit.text.toString()
                        val cumle2 = binding.cumleText2.text.toString()
                        val kullanici = auth.currentUser?.displayName.orEmpty()
                        val tarih = Timestamp.now()

                        if (kelimeIng.isNotEmpty() && kelimeTur.isNotEmpty() && cumle1.isNotEmpty()) {
                            val kelimeMap = hashMapOf<String, Any>(
                                "ingilizceKelime" to kelimeIng,
                                "turkceKarsiligi" to kelimeTur,
                                "birinciCumle" to cumle1,
                                "ikinciCumle" to cumle2,
                                "tarih" to tarih,
                                "kullaniciAdi" to kullanici,
                                "gorselUrl" to downloadUrl
                            )

                            dataBase.collection("kelimeler").add(kelimeMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Kelime başarıyla eklendi.", Toast.LENGTH_SHORT).show()

                                        // Alanları sıfırla
                                        binding.ingilizceKelimeEdit.setText("")
                                        binding.turkceKarsilikEdit.setText("")
                                        binding.cumle1Edit.setText("")
                                        binding.cumleText2.setText("")
                                        binding.gorselEkleBtn.setImageResource(android.R.drawable.ic_menu_gallery)
                                        gorsel = null
                                        bitmap = null

                                        bagla(Sozluk::class.java, false)
                                    }
                                    binding.progressBar3.visibility = View.GONE
                                    binding.kelimeEkleBtn.isEnabled = true
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                                    binding.progressBar3.visibility = View.GONE
                                    binding.kelimeEkleBtn.isEnabled = true
                                }

                        } else {
                            Toast.makeText(this, "Lütfen eksik bilgileri girin.", Toast.LENGTH_SHORT).show()
                            binding.progressBar3.visibility = View.GONE
                            binding.kelimeEkleBtn.isEnabled = true
                        }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    binding.progressBar3.visibility = View.GONE
                    binding.kelimeEkleBtn.isEnabled = true
                }
        } else {
            Toast.makeText(this, "Lütfen görsel seçin.", Toast.LENGTH_SHORT).show()
        }
    }

    fun gorselEkle(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galeriLauncher.launch(galeriIntent)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galeriLauncher.launch(galeriIntent)
            }
        }
    }
}
