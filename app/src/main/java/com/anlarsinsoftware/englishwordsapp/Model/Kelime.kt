package com.anlarsinsoftware.englishwordsapp.Model

import com.google.firebase.firestore.PropertyName
import java.util.Date

class Kelime {


    var kelimeId: String = ""
    var kullaniciAdi: String = ""

    @get:PropertyName("ingilizceKelime")
    @set:PropertyName("ingilizceKelime")
    var kelimeIng: String = ""

    @get:PropertyName("turkceKarsiligi")
    @set:PropertyName("turkceKarsiligi")
    var kelimeTur: String = ""

    var birinciCumle: String = ""
    var ikinciCumle: String = ""
    var gorselUrl: String? = null
    var docId: String = ""
    var dogruSayisi: Int = 0
    var sonDogruMs: Long = 0L
    var asama: Int = 0
    var sonDogruTarih: Date? = null

    constructor()


    constructor(
        kelimeId: String,
        kullaniciAdi: String,
        kelimeIng: String,
        kelimeTur: String,
        birinciCumle: String,
        ikinciCumle: String,
        gorselUrl: String?
    ) {
        this.kelimeId = kelimeId
        this.kullaniciAdi = kullaniciAdi
        this.kelimeIng = kelimeIng
        this.kelimeTur = kelimeTur
        this.birinciCumle = birinciCumle
        this.ikinciCumle = ikinciCumle
        this.gorselUrl = gorselUrl
    }
}
