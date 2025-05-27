package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Adapter.SonYanlisKelimeAdapter
import com.anlarsinsoftware.englishwordsapp.Adapter.UserAdapter
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.Model.User
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.email_intent
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityHomePageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation


class HomePageActivity : BaseCompact() {
    private lateinit var auth:FirebaseAuth
    private lateinit var binding : ActivityHomePageBinding
    private lateinit var leaderBoardRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var sonYanlisAdapter: SonYanlisKelimeAdapter
    private val sonYanlisList = mutableListOf<Kelime>()
    private val userList = mutableListOf<User>()
    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth

       slideFun()
        binding.leaderBoard.setOnClickListener{
            showLeaderBoardBottomSheet()
        }

        binding.yanlisCevaplarRecyclerView.layoutManager = LinearLayoutManager(this)
        sonYanlisAdapter = SonYanlisKelimeAdapter(sonYanlisList)
        binding.yanlisCevaplarRecyclerView.adapter = sonYanlisAdapter

        loadRecentWrongWords()


        binding.yanlisCevaplarRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val menuLayout = findViewById<View>(R.id.MenuLayout)

                if (dy > 10) {
                    menuLayout.animate().translationY(menuLayout.height.toFloat()).setDuration(150)
                } else if (dy < -10) {
                    menuLayout.animate().translationY(0f).setDuration(150)
                }
            }
        })

    }

    private fun showLeaderBoardBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.leaderboard_bottom_sheet, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheetView)

        val recyclerView = bottomSheetView.findViewById<RecyclerView>(R.id.leaderBoardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val bottomSheetAdapter = UserAdapter(userList)
        recyclerView.adapter = bottomSheetAdapter

        // Firebase'den verileri çek
        db.collection("kullanicilar")
            .orderBy("dogruSayisi", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (doc in result) {
                    val kullanici = User(
                        ad = doc.getString("kullaniciAdi") ?: "",
                        dogruSayisi = doc.getLong("dogruSayisi")?.toInt() ?: 0,
                        oran = doc.getString("basariOrani") ?: "0%",
                        profilUrl = doc.getString("profilFotoUrl") ?: ""
                    )
                    userList.add(kullanici)
                }
                bottomSheetAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Liderlik verileri alınamadı", Toast.LENGTH_SHORT).show()
            }

        dialog.show()
    }

    private fun loadRecentWrongWords() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("kullaniciKelimeleri")
            .document(uid)
            .collection("kelimeler")
            .whereEqualTo("asama", 1)
            .orderBy("sonDogruTarih", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { documents ->
                sonYanlisList.clear()
                var counter = documents.size()
                if (counter == 0) {
                    sonYanlisAdapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val kelimeId = doc.id
                    db.collection("kelimeler")
                        .document(kelimeId)
                        .get()
                        .addOnSuccessListener { kelimeDoc ->
                            val kelime = kelimeDoc.toObject(Kelime::class.java)

                            if (kelime != null) {
                                sonYanlisList.add(kelime)
                            }
                            counter--
                            if (counter == 0) {
                                sonYanlisAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }



    fun picassoFun(url:String,imageView:ImageView){
        val radius=50
        val margin=0
        Picasso.get().load(url)
            .transform(RoundedCornersTransformation(radius, margin))
            .placeholder(R.drawable.gallery_icon).into(imageView)

    }



    fun slideFun(){

        val firstFlipImageUrl ="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2FkelimeEkleyin.png?alt=media&token=e8d54213-41cc-44ec-86f8-efe206144133"
        picassoFun(firstFlipImageUrl,binding.firstCardSliderImage)

        val secondFlipImageUrl="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2Fsorularabakin.png?alt=media&token=82afeeeb-7de0-4b1f-b54c-21a9f884cfcd"
        picassoFun(secondFlipImageUrl,binding.secondCardSliderImage)

        val thirdFlipImageUrl="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2FFeedback.png?alt=media&token=ad003234-c474-4cde-bd66-c0af584c8c54"
        picassoFun(thirdFlipImageUrl,binding.thirdCardSliderImage)

        val fourFlipImageUrl="https://firebasestorage.googleapis.com/v0/b/englishwordapp-7fb3b.firebasestorage.app/o/homeScrolPhotos%2Fyapay.png?alt=media&token=922d8a96-cdeb-4e77-ab3a-950049d72c94"
        picassoFun(fourFlipImageUrl,binding.fourCardSliderImage)


        var vFlipper=binding.viewFlipper
        vFlipper.setAutoStart(true)
        vFlipper.setFlipInterval(3500)
        vFlipper.startFlipping()
        binding.firstCardText.text="YENİ KELİMELER EKLEMEK İSTERMİSİN?"
        binding.secondCardText.text="QUİZ İLE KENDİNİ TEST ETMEYE HAZIRMISIN?"
        binding.thirdCardText.text="BİZE GERİ BİLDİRİM GÖNDERİN"
        binding.fourCardText.text="Yapay Zekayı Kullanın"

    }
    fun firstCardImageClick(view: View){
       bagla(WordAddPage::class.java,false)
    }
    fun secondCardImageClick(view: View){
        quizPageClick(view)
    }
    fun thirdCardImageClick(view: View){

        try {
            startActivity(Intent.createChooser(email_intent, "Mail uygulaması seçiniz"))
        } catch (e: Exception) {
            Toast.makeText(this, "Mail uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
        }

    }
    fun fourCardImageClick(view : View){
        bagla(PromptPage::class.java,false)
    }
    fun quizPageClick(view:View){
        bagla(QuizPageActivity::class.java,false)
    }
    fun yapayZekaClick(view: View){
        bagla(PromptPage::class.java,false)
    }
    fun leaderBoardClick(view: View){
      loadLeaderboard()
    }
    fun raporPageClick(view: View){
        bagla(RaporPage::class.java,false)
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

        private fun loadLeaderboard() {
            db.collection("kullanicilar")
                .orderBy("dogruSayisi", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    userList.clear()
                    for (doc in result) {
                        val kullanici = User(
                            ad = doc.getString("kullaniciAdi") ?: "",
                            dogruSayisi = doc.getLong("dogruSayisi")?.toInt() ?: 0,
                            oran = doc.getString("basariOrani") ?: "0%",
                            profilUrl = doc.getString("profilFotoUrl") ?: ""
                        )
                        userList.add(kullanici)
                    }
                    userAdapter.notifyDataSetChanged()
                    leaderBoardRecyclerView.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Liderlik verileri alınamadı", Toast.LENGTH_SHORT).show()
                }
        }
    fun gamePageClick(view: View){bagla(BulmacaOyunu::class.java,false)}





}