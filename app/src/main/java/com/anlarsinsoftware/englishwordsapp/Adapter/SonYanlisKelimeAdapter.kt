package com.anlarsinsoftware.englishwordsapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.Kelime
import com.anlarsinsoftware.englishwordsapp.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.text.SimpleDateFormat
import java.util.Locale

class SonYanlisKelimeAdapter(private val kelimeList: List<Kelime>) :
    RecyclerView.Adapter<SonYanlisKelimeAdapter.KelimeViewHolder>() {

    class KelimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val kelime_image: ImageView = view.findViewById(R.id.kelime_Yimage)
        val turkce: TextView = view.findViewById(R.id.kelime_Ytur)
        val inglizce: TextView = view.findViewById(R.id.kelime_Ying)
        val yanlis_tarihi: TextView = view.findViewById(R.id.yanlisTarihi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_son_yanlis_kelime, parent, false)
        return KelimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: KelimeViewHolder, position: Int) {
        val kelime = kelimeList[position]

        if (!kelime.gorselUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(kelime.gorselUrl)
                .placeholder(R.drawable.gallery_icon)
                .error(R.drawable.false_ico)
                .transform(RoundedCornersTransformation(50, 0))
                .into(holder.kelime_image)
        } else {
            holder.kelime_image.setImageResource(R.drawable.add_circle)
        }

        holder.inglizce.text=kelime.kelimeIng
        holder.turkce.text = kelime.kelimeTur
        kelime.sonDogruTarih?.let {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            holder.yanlis_tarihi.text = "${formatter.format(it)} tarihinde yanlış yapıldı."
        }

    }

    override fun getItemCount(): Int = kelimeList.size
}
