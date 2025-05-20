package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.anlarsinsoftware.englishwordsapp.Adapter.SozlukAdapter
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.databinding.ActivitySozluk2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class Sozluk : BaseCompact() {

    private val db = Firebase.firestore
    private var kelimelerListesi = ArrayList<Kelime>()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySozluk2Binding
    private lateinit var recyclerViewAdapter: SozlukAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySozluk2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupRecyclerView()
        setupSearchView()
        getDataFire()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = SozlukAdapter(kelimelerListesi) { dinle ->
            kelimeDetaylari(dinle)
        }
        binding.recyclerView.adapter = recyclerViewAdapter
    }

    private fun setupSearchView() {
        // Customize SearchView appearance
        val searchIcon = binding.sozlukSearch.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(resources.getColor(R.color.indigo_500))

        val searchText = binding.sozlukSearch.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchText.setTextColor(resources.getColor(android.R.color.black))
        searchText.setHintTextColor(resources.getColor(R.color.gray_600))

        binding.sozlukSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerViewAdapter.filterList(newText.orEmpty())
                return true
            }
        })
    }

    private fun getDataFire() {
        db.collection("kelimeler").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val doc = snapshot.documents
                kelimelerListesi.clear()

                for (document in doc) {
                    try {
                        val kullaniciAdi = document.getString("kullaniciAdi") ?: "bilinmeyen kullanici"
                        val cumle1 = document.getString("birinciCumle") ?: "girilmemis birinci cumle"
                        val cumle2 = document.getString("ikinciCumle") ?: "girilmemis ikinci cumle"
                        val kelimeIng = document.getString("ingilizceKelime") ?: "bilinmeyen ingilizce kelime"
                        val kelimeTur = document.getString("turkceKarsiligi") ?: "bilinmeyen turkce kelime"
                        val gorselUrl = document.getString("gorselUrl")

                        if (!gorselUrl.isNullOrEmpty()) {
                            val indirilenKelime = Kelime(
                                "id",
                                kullaniciAdi,
                                kelimeIng.trim(),
                                kelimeTur.trim(),
                                cumle1,
                                cumle2,
                                gorselUrl
                            )
                            kelimelerListesi.add(indirilenKelime)
                        }
                    } catch (ex: Exception) {
                        Toast.makeText(this, "Veri okunurken hata oluştu: ${ex.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                kelimelerListesi.sortBy { it.kelimeIng?.lowercase() }
                recyclerViewAdapter.updateFullList(kelimelerListesi)
            }
        }
    }

    private fun kelimeDetaylari(kelime: Kelime) {
        val view = layoutInflater.inflate(R.layout.detaylar_dialog, null).apply {
            findViewById<TextView>(R.id.ingilizceKelime).text = kelime.kelimeIng
            findViewById<TextView>(R.id.turkceKarsilik).text = kelime.kelimeTur
            findViewById<TextView>(R.id.cumleText1).text = kelime.birinciCumle
            findViewById<TextView>(R.id.cumleText2).text = kelime.ikinciCumle

            val kelimeImageView = findViewById<ImageView>(R.id.kelimeResim)
            if (!kelime.gorselUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(kelime.gorselUrl)
                    .placeholder(R.drawable.gallery_icon)
                    .error(R.drawable.false_ico)
                    .transform(RoundedCornersTransformation(50, 0))
                    .into(kelimeImageView)
            } else {
                kelimeImageView.setImageResource(R.drawable.add_circle)
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .create()
            .apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                show()
            }
    }

    fun backImageClick2(view: View) {
        bagla(HomePageActivity::class.java, false)
    }
}