package com.anlarsinsoftware.englishwordsapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        val card : CardView=itemView.findViewById(R.id.card_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val adText = holder.adText
        val puanTextView=holder.puanText
        val cardItem = holder.card



        adText.text = user.ad
        puanTextView.text = "Doğru: ${user.dogruSayisi}"
        val ranktext= holder.rankText



        when (position) {
            0 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.birincilik)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                ranktext.setCompoundDrawables(icon, null, null, null)
                ranktext.text = ""
                cardItem.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.light_blue_900)
                adText.setTextColor(ContextCompat.getColorStateList(holder.itemView.context,R.color.ikincilik))
                puanTextView.setTextColor(ContextCompat.getColorStateList(holder.itemView.context,R.color.ikincilik))

            }
            1 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ikincilik)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                ranktext.setCompoundDrawables(icon, null, null, null)
                ranktext.text = ""
                cardItem.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.light_blue_600)
            }
            2 -> {
                val icon = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ucunculuk)
                icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                ranktext.setCompoundDrawables(icon, null, null, null)
                ranktext.text = ""
                cardItem.backgroundTintList =
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.light_blue_A200)
            }
            else -> {
                ranktext.setCompoundDrawables(null, null, null, null)
                ranktext.text = "${position + 1}."
            }
        }

    }

    override fun getItemCount(): Int = userList.size
}
