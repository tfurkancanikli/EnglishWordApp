package com.anlarsinsoftware.englishwordsapp.Util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.anlarsinsoftware.englishwordsapp.Util.bagla
import com.anlarsinsoftware.englishwordsapp.ViewPages.BulmacaOyunu
import com.anlarsinsoftware.englishwordsapp.ViewPages.HomePageActivity
import com.anlarsinsoftware.englishwordsapp.ViewPages.ProfileActivity
import com.anlarsinsoftware.englishwordsapp.ViewPages.Sozluk
import com.anlarsinsoftware.englishwordsapp.ViewPages.WordAddPage

open class BaseCompact : AppCompatActivity() {

    fun goToWordAddClick(view: View) {
      bagla(WordAddPage::class.java,false)
    }

    fun goToWordsClick(view: View) {
      bagla(Sozluk::class.java,false)
    }

    fun goToHomeClick(view: View) {
        bagla(HomePageActivity::class.java,false)
    }

    fun goToGameClick(view: View) {
        bagla(BulmacaOyunu::class.java,false)
    }

    fun goToProfileClick(view: View) {
        bagla(ProfileActivity::class.java,false)
    }
    open fun backImageClick(view: View){
        bagla(HomePageActivity::class.java,true)
    }

}
