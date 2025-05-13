package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.anlarsinsoftware.englishwordsapp.Entrance.BaseCompact
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRaporPageBinding
import java.io.File
import java.io.FileOutputStream

class RaporPage : BaseCompact() {

    private lateinit var binding: ActivityRaporPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaporPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun btnPdf(view: View) {
        val dogruSayisi = "0"
        val yanlisSayisi = "0"
        val basariOrani = "0%"
        val enSonYanlisTarih = "Bilinmiyor"

        //PDF GÖRÜNÜMÜ
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("RAPOR SONUÇLARI", 297.5f, 60f, paint)
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 70f, 545f, 70f, paint)
        paint.textSize = 16f
        paint.isFakeBoldText = false
        paint.textAlign = android.graphics.Paint.Align.LEFT

        // VERİLERİ EKLEDİM
        val bilgiler = listOf(
            "Doğru Sayısı: $dogruSayisi",
            "Yanlış Sayısı: $yanlisSayisi",
            "Başarı Oranı: $basariOrani",
            "En Son Yanlış Yapılan Tarih: $enSonYanlisTarih"
        )
        // VERİYİ YAZDIR
        var yOffset = 120f
        bilgiler.forEach {
            canvas.drawText(it, 60f, yOffset, paint)
            yOffset += 30f
        }
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText("ENGLISH WORDS APP", 297.5f, 800f, paint)
        pdfDocument.finishPage(page)

        val file = File(getExternalFilesDir(null), "rapor.pdf")

        try {
            FileOutputStream(file).use { output ->
                pdfDocument.writeTo(output)
            }
            pdfDocument.close()


            val uri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "PDF oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
//ŞİMDİLİK 0 DEĞERİ ATADIM.FİREBASTEN ALINACAĞIMIZ  ZAMAN FARKLI YAZILACAK

