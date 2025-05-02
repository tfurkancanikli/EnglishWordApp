package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityHomePageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso


class HomePageActivity : BaseCompact() {
    private lateinit var auth:FirebaseAuth
    private lateinit var binding : ActivityHomePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth

       slideFun()
    }


    fun picassoFun(url:String,imageView:ImageView){
        Picasso.get().load(url).placeholder(R.drawable.gallery_icon).into(imageView)
    }
    fun slideFun(){
        val firstFlipImageUrl ="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2FkelimeEkleyin.png?alt=media&token=e8d54213-41cc-44ec-86f8-efe206144133"
        picassoFun(firstFlipImageUrl,binding.firstCardSliderImage)

        val secondFlipImageUrl="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2Fsorularabakin.png?alt=media&token=82afeeeb-7de0-4b1f-b54c-21a9f884cfcd"
        picassoFun(secondFlipImageUrl,binding.secondCardSliderImage)

        val thirdFlipImageUrl="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2FFeedback.png?alt=media&token=ad003234-c474-4cde-bd66-c0af584c8c54"
        picassoFun(thirdFlipImageUrl,binding.thirdCardSliderImage)




        var vFlipper=binding.viewFlipper

        vFlipper.setAutoStart(true)
        vFlipper.setFlipInterval(3500)
        vFlipper.startFlipping()

        binding.firstCardText.text="YENİ KELİMELER EKLEMEK İSTERMİSİN?"
        binding.secondCardText.text="QUİZ İLE KENDİNİ TEST ETMEYE HAZIRMISIN?"
        binding.thirdCardText.text="BİZE GERİ BİLDİRİM GÖNDERİN"


    }
    fun firstCardImageClick(view: View){
       bagla(WordAddPage::class.java,false)
    }
    fun secondCardImageClick(view: View){
        goToQuiz(view)
    }
    fun thirdCardImageClick(view: View){
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("mahmutconger@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim")
            putExtra(Intent.EXTRA_TEXT, "Uygulama hakkında geri bildiriminizi buraya yazabilirsiniz.")
        }
        try {
            startActivity(Intent.createChooser(intent, "Mail uygulaması seçiniz"))
        } catch (e: Exception) {
            Toast.makeText(this, "Mail uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
        }

    }
    fun goToQuiz(view:View){
        bagla(QuizPageActivity::class.java,false)
    }
    @Suppress("MissingSuperCall")
    override fun onBackPressed() {

        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Uygulama Kapatılsın mı?")
            .setMessage("Çıkış yapmak istediğinizden emin misiniz?")
            .setPositiveButton("Evet") { dialog, _ ->
                dialog.dismiss()
                finishAffinity()
            }
            .setNegativeButton("Hayır") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }


}