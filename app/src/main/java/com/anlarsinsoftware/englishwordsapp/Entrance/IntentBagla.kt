package com.anlarsinsoftware.englishwordsapp.Entrance

import android.app.Activity
import android.content.Intent

fun Activity.bagla(activityClass: Class<*>){
    val intent = Intent (this,activityClass)
    startActivity(intent)
    finish()
}