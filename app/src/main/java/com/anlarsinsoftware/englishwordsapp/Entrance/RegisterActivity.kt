package com.anlarsinsoftware.englishwordsapp.Entrance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.ViewPages.HomePageActivity
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
    }


    fun kayitolClick(view: View) {

        val email = binding.emailText.text.toString().trim()
        val userName = binding.nameText.text.toString().trim()
        val password = binding.passwordText.text.toString().trim()
        val passwordVerify = binding.passwordVerifyText.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty() && password == passwordVerify) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val guncelKullanici = auth.currentUser
                        val profilGuncelleme= userProfileChangeRequest {
                            displayName = userName
                        }

                            guncelKullanici!!.updateProfile(profilGuncelleme).addOnCompleteListener{task->
                                if(task.isSuccessful){
                                    Toast.makeText(this,"Kullanıcı Adı Güncellendi",Toast.LENGTH_SHORT).show()
                                }


                        }

                        Toast.makeText(applicationContext, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                        bagla(HomePageActivity::class.java,true)
                    } else {
                        Toast.makeText(applicationContext, "Kayıt Başarısız! Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Lütfen tüm alanları doldurun ve şifreleri eşleştirin.", Toast.LENGTH_SHORT).show()
        }

    }
    fun backToSign(view:View){
       bagla(SignInActivity::class.java,true)
    }
}
