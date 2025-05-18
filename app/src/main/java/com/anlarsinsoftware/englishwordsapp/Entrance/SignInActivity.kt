@file:Suppress("DEPRECATION")

package com.anlarsinsoftware.englishwordsapp.Entrance
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.ViewPages.HomePageActivity
import com.anlarsinsoftware.englishwordsapp.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.anlarsinsoftware.englishwordsapp.R
class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // values/strings.xml'de tanımlı olmalı
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.imageView6.setOnClickListener {
            googleGirisYap()
        }


        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null) {
            bagla(HomePageActivity::class.java, true)
        }
    }

    private fun googleGirisYap() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google girişi başarısız: ${e.message}", Toast.LENGTH_SHORT).show()




            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google ile giriş başarılı!", Toast.LENGTH_SHORT).show()
                    bagla(HomePageActivity::class.java, true)
                } else {
                    Toast.makeText(this, "Google ile giriş başarısız!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun startTextClick(view: View) {
        val email = binding.emailText.text.toString().trim()
        val password = binding.passwordText.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        bagla(HomePageActivity::class.java, true)
                    } else {
                        Toast.makeText(this, "Giriş Başarısız! Tekrar Deneyin.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
        }
    }

    fun signUpClick(view: View) {
        bagla(RegisterActivity::class.java, true)
    }

    fun resetPassword(view: View) {
        val email = binding.emailText.text.toString().trim()
        if (email.isNotEmpty()) {
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                "Lütfen kayıtlı e-posta adresinizi giriniz",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun bagla(hedefSinif: Class<*>, finishCurrent: Boolean = false) {
        startActivity(Intent(this, hedefSinif))
        if (finishCurrent) finish()
    }
}