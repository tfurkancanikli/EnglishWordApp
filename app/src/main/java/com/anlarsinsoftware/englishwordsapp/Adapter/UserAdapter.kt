package com.anlarsinsoftware.englishwordsapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anlarsinsoftware.englishwordsapp.Model.User
import com.anlarsinsoftware.englishwordsapp.R
import com.squareup.picasso.Picasso

class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adText: TextView = itemView.findViewById(R.id.adText)
        val puanText: TextView = itemView.findViewById(R.id.puanText)
        val rankText : TextView=itemView.findViewById(R.id.rankText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]


        holder.adText.text = user.ad
        holder.puanText.text = "Doğru: ${user.dogruSayisi}"


        // Sıra numarası kısmı
        when (position) {
            0 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.birincilik)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                holder.rankText.setCompoundDrawables(icon, null, null, null)
                holder.rankText.text = "" // Yazı yok, sadece ikon
            }
            1 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ikincilik)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                holder.rankText.setCompoundDrawables(icon, null, null, null)
                holder.rankText.text = "" // Yazı yok, sadece ikon
            }
            2 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ucunculuk)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                holder.rankText.setCompoundDrawables(icon, null, null, null)
                holder.rankText.text = "" // Yazı yok, sadece ikon
            }
            else -> {
                holder.rankText.setCompoundDrawables(null, null, null, null)
                holder.rankText.text = "${position + 1}."
            }
        }



    }

    override fun getItemCount(): Int = userList.size
}
