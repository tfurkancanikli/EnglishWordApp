package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.anlarsinsoftware.englishwordsapp.Adapter.SozlukAdapter
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivitySozluk2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class Sozluk : BaseCompact() {

    val db = Firebase.firestore
    var kelimelerListesi = ArrayList<Kelime>()
    private  lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivitySozluk2Binding
    private lateinit var recyclerViewAdapter : SozlukAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySozluk2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= Firebase.auth
        getDataFire()

        var layotManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager=layotManager
        recyclerViewAdapter= SozlukAdapter(kelimelerListesi){
            dinle ->kelimeDetaylari(dinle)
        }
        binding.recyclerView.adapter=recyclerViewAdapter

    }

    fun getDataFire(){
        db.collection("kelimeler").addSnapshotListener{snapshot,e->
            if(e!=null){
                Toast.makeText(this,e.localizedMessage,Toast.LENGTH_LONG).show()
            }else{
                if (snapshot!=null){
                    if(!snapshot.isEmpty) {
                        val doc = snapshot.documents
                        kelimelerListesi.clear()
                        for (document in doc){
                            val kullaniciAdi=document.get("kullaniciAdi")as? String?:"bilinmeyen kullanici"
                            val cumle1=document.get("birinciCumle")as? String?:"girilmemis birinci cumle"
                            val cumle2=document.get("ikinciCumle")as? String?:"girilmemis ikinci cumle"
                            val kelimeIng= document.get("ingilizceKelime")as? String?:"bilinmeyen ingilizce kelime"
                            val kelimeTur=document.get("turkceKarsiligi")as? String?:"bilinmeyen turkce kelime"
                            val tarih = document.get("tarih")as? String?:"bilinmeyen tarih"
                            val gorselUrl=document.get("gorselUrl")as? String?:"bilinmeyen resim"


                            var indirilenKelime= Kelime(kullaniciAdi,kelimeIng,kelimeTur,cumle1,cumle2,gorselUrl)
                            kelimelerListesi.add(indirilenKelime)


                        }
                        kelimelerListesi.sortBy { it.kelimeIng?.lowercase() }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        }





    fun kelimeEkleSayfa(view : View){
        bagla(WordAddPage::class.java,false)
    }
    fun backToHome(view:View){
       // bagla(HomePageActivity::class.java)
    }

    fun kelimeDetaylari(kelime: Kelime) {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.detaylar_dialog, null)

        val ingilizceKelimeTextView = view.findViewById<TextView>(R.id.ingilizceKelime)
        val turkceKarsilikTextView = view.findViewById<TextView>(R.id.turkceKarsilik)
        val cumleText1TextView = view.findViewById<TextView>(R.id.cumleText1)
        val cumleText2TextView = view.findViewById<TextView>(R.id.cumleText2)
        val kelimeImageView = view.findViewById<ImageView>(R.id.kelimeResim)

        ingilizceKelimeTextView.text = kelime.kelimeIng
        turkceKarsilikTextView.text = kelime.kelimeTur
        cumleText1TextView.text = kelime.cumle1
        cumleText2TextView.text = kelime.cumle2
        Picasso.get().load(kelime.gorselUrl).into(kelimeImageView)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.show()
    }


}