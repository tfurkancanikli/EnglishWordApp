package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.anlarsinsoftware.englishwordsapp.Adapter.SozlukAdapter
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
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
import java.util.Locale

class Sozluk : BaseCompact(),TextToSpeech.OnInitListener {

    private val db = Firebase.firestore
    private var kelimelerListesi = ArrayList<Kelime>()
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySozluk2Binding
    private lateinit var recyclerViewAdapter: SozlukAdapter
    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySozluk2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        setupRecyclerView()
        setupSearchView()
        getDataFire()


        tts = TextToSpeech(this,this)

    }


    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerViewAdapter = SozlukAdapter(kelimelerListesi) { dinle ->
            kelimeDetaylari(dinle)
        }
        binding.recyclerView.adapter = recyclerViewAdapter
    }

    private fun setupSearchView() {
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
                        val docId = document.id // 🔥 Belge ID'sini al

                        if (!gorselUrl.isNullOrEmpty()) {
                            val indirilenKelime = Kelime(
                                docId, // ← burada gerçek belge ID’si veriliyor
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
        val view = layoutInflater.inflate(R.layout.detaylar_dialog, null)
        val close = view.findViewById<ImageView>(R.id.close)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        close.setOnClickListener {
            alertDialog.dismiss()
        }

        val kelimeIng = view.findViewById<TextView>(R.id.ingilizceKelimeEdit)
        val kelimeTur = view.findViewById<TextView>(R.id.turkceKarsilikEdit)
        val cumle1 = view.findViewById<TextView>(R.id.cumle1_edit)
        val cumle2 = view.findViewById<TextView>(R.id.cumleText2)
        val kelimeImageView = view.findViewById<ImageView>(R.id.kelimeResim)

        kelimeIng.text = kelime.kelimeIng
        kelimeTur.text = kelime.kelimeTur
        cumle1.text = kelime.birinciCumle
        cumle2.text = kelime.ikinciCumle

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

       kelimeIng.setOnClickListener {
            if (::tts.isInitialized) tts.speak(kelime.kelimeIng, TextToSpeech.QUEUE_FLUSH, null, null)

        }
        cumle1.setOnClickListener {
            if (::tts.isInitialized) tts.speak(kelime.birinciCumle, TextToSpeech.QUEUE_FLUSH, null, null)
        }
        cumle2.setOnClickListener {
            if (::tts.isInitialized) tts.speak(kelime.ikinciCumle, TextToSpeech.QUEUE_FLUSH, null, null)
        }


        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val db = Firebase.firestore
        val silbtn = view.findViewById<ImageView>(R.id.btnSil)
        val duzenlebtn = view.findViewById<ImageView>(R.id.btnDuzenle)

        if (currentUserId != null) {
            db.collection("kullanicilar").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val isAdmin = document.getBoolean("isAdmin") ?: false
                    if (isAdmin) {
                        silbtn.setOnClickListener {
                            AlertDialog.Builder(this)
                                .setTitle("Silmek İşlemi")
                                .setMessage("Silmek istediğinden emin misin?")
                                .setPositiveButton("Sil") { dialog, _ ->
                                    kelimeSil(kelime)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("İptal") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }

                        duzenlebtn.setOnClickListener {
                            AlertDialog.Builder(this)
                                .setTitle("Düzenleme İşlemi")
                                .setMessage("Düzenlemek istediğinden emin misin?")
                                .setPositiveButton("Düzenle") { dialog, _ ->
                                    kelimeDuzenle(kelime)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("İptal") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    } else {
                        silbtn.visibility = View.GONE
                        duzenlebtn.visibility = View.GONE
                    }
                }
        }

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun kelimeSil(kelime: Kelime) {
        // Firestore'da kelimeyi sil (id'yi kullanmalısın, örnek sabit "id" değil)
        db.collection("kelimeler")
            .whereEqualTo("ingilizceKelime", kelime.kelimeIng) // daha iyi: gerçek id alanı
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("kelimeler").document(document.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kelime silindi", Toast.LENGTH_SHORT).show()
                            kelimelerListesi.remove(kelime)
                            recyclerViewAdapter.updateFullList(kelimelerListesi)
                        }
                }
            }
    }


    private fun kelimeDuzenle(kelime: Kelime) {
        // Yeni bir AlertDialog aç, editTextlerle güncelleme yap
        val editView = layoutInflater.inflate(R.layout.edit_kelime_dialog, null)
        val ingEdit = editView.findViewById<TextView>(R.id.ingilizceKelimeEdit)
        val turkEdit = editView.findViewById<TextView>(R.id.turkceKarsilikEdit)
        val cumle1Edit = editView.findViewById<TextView>(R.id.cumle1_edit)
        val cumle2Edit = editView.findViewById<TextView>(R.id.cumleText2)

        // mevcut veriyi göster
        ingEdit.text = kelime.kelimeIng
        turkEdit.text = kelime.kelimeTur
        cumle1Edit.text = kelime.birinciCumle
        cumle2Edit.text = kelime.ikinciCumle

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)

         dialog.setView(editView)
            .setPositiveButton("Güncelle") { _, _ ->
                val yeniIng = ingEdit.text.toString()
                val yeniTurk = turkEdit.text.toString()
                val yeniCumle1 = cumle1Edit.text.toString()
                val yeniCumle2 = cumle2Edit.text.toString()

                // Firestore'da güncelle
                db.collection("kelimeler")
                    .whereEqualTo("ingilizceKelime", kelime.kelimeIng)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            db.collection("kelimeler").document(doc.id).update(
                                mapOf(
                                    "ingilizceKelime" to yeniIng,
                                    "turkceKarsiligi" to yeniTurk,
                                    "birinciCumle" to yeniCumle1,
                                    "ikinciCumle" to yeniCumle2
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(this, "Kelime güncellendi", Toast.LENGTH_SHORT).show()
                                getDataFire()
                                dialog.setOnDismissListener{dis->
                                    dis.dismiss()
                                }

                            }
                        }

                    }
                dialog.setOnDismissListener{dis->
                    dis.dismiss()
                }
            }
            .setNegativeButton("İptal", null)
            .create()

        dialog.show()
    }





    fun backImageClick2(view: View) {
        bagla(HomePageActivity::class.java, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK // ya da Locale.US
        }
    }
}