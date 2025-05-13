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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

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
                            val gorselUrl = document.get("gorselUrl") as? String

                            if (gorselUrl!=null){

                                var indirilenKelime= Kelime("ıd",kullaniciAdi,kelimeIng,kelimeTur,cumle1,cumle2, gorselUrl)
                                kelimelerListesi.add(indirilenKelime)
                            }else{
                                Toast.makeText(this,"Kelime yüklenirken hata oluştu",Toast.LENGTH_LONG).show()
                            }

                        }
                        kelimelerListesi.sortBy { it.kelimeIng?.lowercase() }
                        recyclerViewAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
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
        cumleText1TextView.text = kelime.birinciCumle
        cumleText2TextView.text = kelime.ikinciCumle
        val radius=50
        val margin=0

        if (!kelime.gorselUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(kelime.gorselUrl)
                .placeholder(R.drawable.gallery_icon)
                .error(R.drawable.false_ico)
                .transform(RoundedCornersTransformation(radius, margin))
                .into(kelimeImageView)
        } else {
            kelimeImageView.setImageResource(R.drawable.add_circle)
        }
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.show()
    }


}