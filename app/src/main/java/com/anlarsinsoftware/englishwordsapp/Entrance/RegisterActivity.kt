package com.anlarsinsoftware.englishwordsapp.Entrance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.ViewPages.HomePageActivity
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.anlarsinsoftware.englishwordsapp.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_UP = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.imageView5.setOnClickListener {
            googleSignUp()
        }
    }


    private fun googleSignUp() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_UP)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_UP) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google ile kayıt başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdate = userProfileChangeRequest {
                        displayName = user?.displayName ?: "Google Kullanıcısı"
                    }
                    user?.updateProfile(profileUpdate)

                    Toast.makeText(this, "Google ile kayıt başarılı!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomePageActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
                        val profilGuncelleme = userProfileChangeRequest {
                            displayName = userName
                        }

                        guncelKullanici!!.updateProfile(profilGuncelleme).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Kullanıcı Adı Güncellendi", Toast.LENGTH_SHORT).show()
                            }
                        }

                        Toast.makeText(applicationContext, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                        bagla(HomePageActivity::class.java, true)
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

    fun backToSign(view: View) {
        bagla(SignInActivity::class.java, true)
    }

    private fun bagla(activity: Class<*>, finish: Boolean) {
        startActivity(Intent(this, activity))
        if (finish) finish()
    }
}