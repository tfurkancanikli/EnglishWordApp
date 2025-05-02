package com.anlarsinsoftware.englishwordsapp.Entrance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.ViewPages.HomePageActivity
import com.anlarsinsoftware.englishwordsapp.databinding.ActivitySignInBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()

        val guncelKullanici = auth.currentUser
        if (guncelKullanici!=null){
           bagla(HomePageActivity::class.java,true)
        }
    }

    fun startTextClick(view: View){

        val email = binding.emailText.text.toString().trim()
        val password = binding.passwordText.text.toString().trim()


        if (email.isNotEmpty()&&password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        Toast.makeText(this,"Giriş Başarılı !",Toast.LENGTH_SHORT).show()
                        bagla(HomePageActivity::class.java,true)
                    }
                    else{
                        Toast.makeText(this,"Giriş Başarısız Tekrar Deneyin!",Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener{e->
                    Toast.makeText(this,e.localizedMessage,Toast.LENGTH_SHORT).show()
                }} else{
            Toast.makeText(this,"Lütfen Boşluk Bırakmayın!",Toast.LENGTH_SHORT).show()
        }
    }

    fun signUpClick(view : View){
       bagla(RegisterActivity::class.java,true)
    }
    fun resetPassword(view:View){

        val email = binding.emailText.text.toString().trim()
        if (email.isNotEmpty()) {
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Şifre sifirlama bağlantisi girilen mailinize iletilmiştir.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
        else{
            Toast.makeText(
                this,
                "Lütfen kayıtlı email giriniz",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}