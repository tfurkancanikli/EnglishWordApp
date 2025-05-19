package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.anlarsinsoftware.englishwordsapp.Util.BaseCompact
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRaporPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RaporPage : BaseCompact() {
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImage: ImageView
    private lateinit var textName: TextView
    private lateinit var binding: ActivityRaporPageBinding
    private lateinit var kimlikDogrulama: FirebaseAuth
    private lateinit var veritabani: FirebaseFirestore
    private var kullaniciAdi: String = ""
    private var dogruSayisi: Int = 0
    private var yanlisSayisi: Int = 0
    private var basariOrani: String = "0%"
    private var sonDogruTarihi: String = "Henüz kayıt yok"
    private var toplamKelimeSayisi: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivityRaporPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileImage = findViewById(R.id.profileImage2)
        textName = binding.textName2
        kimlikDogrulama = FirebaseAuth.getInstance()
        veritabani = FirebaseFirestore.getInstance()
        val mevcutKullanici = kimlikDogrulama.currentUser

        if (mevcutKullanici != null) {
            kullaniciAdi = mevcutKullanici.email ?: "Misafir Kullanıcı"
            kullaniciVerileriniYukle(mevcutKullanici.uid)
        } else {
            Toast.makeText(this, "Kullanıcı girişi yapılmamış", Toast.LENGTH_SHORT).show()
            finish()
        }
        val currentUser = auth.currentUser
        currentUser?.let {
            textName.text = it.displayName ?: "İsim girilmedi"
            it.photoUrl?.let { uri ->
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.baseline_person_24)
                    .into(profileImage)
            }
        }
    }
    //Giriş yapan kullanıcı verilerini alır
    private fun kullaniciVerileriniYukle(kullaniciId: String) {
        veritabani.collection("kullaniciKelimeleri")
            .document(kullaniciId)
            .collection("kelimeler")
            .get()
            .addOnSuccessListener { belgeler ->
                toplamKelimeSayisi = belgeler.size()
                istatistikleriHesapla(belgeler)
                arayuzuGuncelle()

                updateKullanici(
                    kullaniciId,
                    dogruSayisi,
                    yanlisSayisi,
                    basariOrani,
                    sonDogruTarihi,
                    toplamKelimeSayisi
                )

                Log.d("RaporSayfasi", "Toplam $toplamKelimeSayisi kelime bulundu")
            }
            .addOnFailureListener { hata ->
                Toast.makeText(this, "Kelime listesi alınamadı", Toast.LENGTH_SHORT).show()
                Log.e("RaporSayfasi", "Kelime listesi alınamadı", hata)
            }
    }
    //Verileri hesaplar
    private fun istatistikleriHesapla(belgeler: QuerySnapshot) {
        var toplamDogruSayisi = 0
        var toplamYanlisSayisi= 0
        var enSonDogruTarih: Date? = null

        for (belge in belgeler) {
            toplamDogruSayisi += belge.getLong("dogruSayac")?.toInt() ?: 0
            toplamYanlisSayisi += belge.getLong("yanlisSayac")?.toInt() ?: 0

            val dogruTarih = belge.getTimestamp("sonDogruTarih")?.toDate()
            if (dogruTarih != null && (enSonDogruTarih == null || dogruTarih.after(enSonDogruTarih))) {
                enSonDogruTarih = dogruTarih
            }
        }

        dogruSayisi = toplamDogruSayisi
        //Yanlış sayısı daha az veri açısından toplamdan doğru sayısının çıkarılmasıyla bulunur.
        yanlisSayisi = toplamYanlisSayisi

        basariOrani = if (toplamKelimeSayisi > 0) {
            val oran = (dogruSayisi * 100) / (dogruSayisi+yanlisSayisi)
            "%$oran"
        } else {
            "0%"
        }

        //BURAYA

        sonDogruTarihi = enSonDogruTarih?.let {
            SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr")).format(it)
        } ?: "Henüz kayıt yok"
    }

    private fun arayuzuGuncelle() {
        binding.apply {
            dDogru.text = "Doğru Sayısı: $dogruSayisi"
            yYanlis.text = "Yanlış Sayısı: $yanlisSayisi"
            oOran.text = "Başarı Oranı: $basariOrani"
            tTarih.text = "Son Doğru Tarihi: $sonDogruTarihi"
        }
    }

    private fun updateKullanici(
        kullaniciId: String,
        dogruSayisi: Int,
        yanlisSayisi: Int,
        basariOran: String,
        sonDogruTarihi: String,
        toplamKelime: Int
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val firestore = Firebase.firestore

        val kullaniciVerisi = hashMapOf(
            "uid" to kullaniciId,
            "kullaniciAdi" to (currentUser.displayName ?: "Bilinmiyor"),
            "email" to (currentUser.email ?: "Email yok"),
            "dogruSayisi" to dogruSayisi,
            "yanlisSayisi" to yanlisSayisi,
            "basariOrani" to basariOran,
            "sonDogruTarihi" to sonDogruTarihi,
            "toplamKelimeSayisi" to toplamKelime,
            "profilFotoUrl" to (currentUser.photoUrl?.toString() ?: "")
        )

        firestore.collection("kullanicilar")
            .document(kullaniciId)
            .set(kullaniciVerisi)
            .addOnSuccessListener {
                Log.d("RaporSayfasi", "Kullanıcı verisi başarıyla güncellendi")
            }
            .addOnFailureListener { e ->
                Log.e("RaporSayfasi", "Kullanıcı verisi güncellenemedi", e)
            }
    }

    fun pdfOlustur(view: View) {
        val pdfDokumani = PdfDocument()
        val sayfaBilgisi = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val sayfa = pdfDokumani.startPage(sayfaBilgisi)
        val cerceve = sayfa.canvas
        val cizim = android.graphics.Paint()

        cizim.textSize = 24f
        cizim.isFakeBoldText = true
        cizim.textAlign = android.graphics.Paint.Align.CENTER
        cerceve.drawText("İNGİLİZCE KELİME RAPORU", 297.5f, 60f, cizim)

        cizim.strokeWidth = 2f
        cerceve.drawLine(50f, 80f, 545f, 80f, cizim)

        cizim.textSize = 16f
        cizim.isFakeBoldText = false
        cizim.textAlign = android.graphics.Paint.Align.LEFT
        //Görüntülenecek verilerin listelenmesi.
        val bilgiler = listOf(
            "Kullanıcı: $kullaniciAdi",
            "Toplam Kelime: $toplamKelimeSayisi",
            "Toplam Doğru: $dogruSayisi",
            "Toplam Yanlış: $yanlisSayisi",
            "Başarı Oranı: $basariOrani",
            "Son Doğru Tarihi: $sonDogruTarihi"
        )
        var yKonumu = 120f
        bilgiler.forEach { bilgi ->
            cerceve.drawText(bilgi, 60f, yKonumu, cizim)
            yKonumu += 30f
        }
        cizim.textSize = 14f
        cizim.isFakeBoldText = true
        cizim.textAlign = android.graphics.Paint.Align.CENTER

        val tarih = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date())
        cerceve.drawText("English Words App - $tarih", 297.5f, 800f, cizim)

        pdfDokumani.finishPage(sayfa)

        val dosyaAdi = "Ingilizce_Kelime_Raporu_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.pdf"
        val dosya = File(getExternalFilesDir(null), dosyaAdi)
        // Try-catch ile hata yakalama algoritmasının kurulması.
        try {
            FileOutputStream(dosya).use { cikti ->
                pdfDokumani.writeTo(cikti)
                Toast.makeText(this, "PDF oluşturuldu", Toast.LENGTH_SHORT).show()
            }
            pdfDokumani.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                dosya
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "PDF oluşturulamadı", Toast.LENGTH_LONG).show()
            Log.e("RaporSayfasi", "PDF oluşturma hatası", e)
        }
    }
}
