package com.anlarsinsoftware.englishwordsapp.ViewPages

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.Entrance.bagla
import com.anlarsinsoftware.englishwordsapp.R
import com.anlarsinsoftware.englishwordsapp.databinding.ActivityRaporPageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.anlarsinsoftware.englishwordsapp.Entrance.SignInActivity
import java.io.File
import java.io.FileOutputStream

class RaporPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityRaporPageBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var reportLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaporPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth= Firebase.auth
        db = FirebaseFirestore.getInstance()
    }
    fun btnPdf(view: View) {

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        paint.textSize = 14f
        paint.color = android.graphics.Color.BLACK


        canvas.drawText("Doğru Sayısı: ${binding.dDogru.text}", 10f, 25f, paint)
        canvas.drawText("Yanlış Sayısı: ${binding.yYanlis.text}", 10f, 50f, paint)
        canvas.drawText("Başarı Oranı: ${binding.oOran.text}", 10f, 75f, paint)
        canvas.drawText("En Son Yanlış Yapılan Tarih: ${binding.tTarih.text}", 10f, 100f, paint)

        pdfDocument.finishPage(page)


        val file = File(getExternalFilesDir(null), "rapor.pdf")

        try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()


            val uri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "PDF oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
    fun btnBack(view:View){
        bagla(ProfileActivity::class.java)
    }

}





