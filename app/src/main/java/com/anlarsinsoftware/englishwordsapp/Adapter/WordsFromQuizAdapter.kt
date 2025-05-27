package com.anlarsinsoftware.englishwordsapp.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WordsFromQuizAdapter(private val kelimeList: List<Kelime>) :
    RecyclerView.Adapter<WordsFromQuizAdapter.KelimeViewHolder>() {

    class KelimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val turkce: TextView = view.findViewById(R.id.tv_kelimeTur)
        val inglizce: TextView = view.findViewById(R.id.tv_kelimeIng)
        val tarih : TextView=view.findViewById(R.id.tarih)
        val asama : TextView=view.findViewById(R.id.kelimeAsama)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_words, parent, false)
        return KelimeViewHolder(view)

    }

    override fun onBindViewHolder(holder: KelimeViewHolder, position: Int) {
        val kelime = kelimeList[position]

        holder.inglizce.text = kelime.kelimeIng
        holder.turkce.text = kelime.kelimeTur

        if (kelime.asama >= 6) {
            holder.asama.text = "Öğrenildi."
        } else {
            holder.asama.text = kelime.asama.toString()
        }

        kelime.sonDogruTarih?.let {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            holder.tarih.text = formatter.format(it)
        }


    }

    override fun getItemCount(): Int = kelimeList.size
}
