package com.anlarsinsoftware.englishwordsapp.Util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import com.anlarsinsoftware.englishwordsapp.R

fun Activity.bagla(activityClass: Class<*>,isFinished : Boolean){

    if (isFinished==false){
        val intent = Intent (this,activityClass)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent,options.toBundle())
    }else{
        val intent = Intent (this,activityClass)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent,options.toBundle())
        finish()
    }


}