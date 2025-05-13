package com.anlarsinsoftware.englishwordsapp.Model

class Kelime {

    var kelimeId: String = ""
    var kullaniciAdi: String = ""
    var kelimeIng: String = ""
    var kelimeTur: String = ""
    var birinciCumle: String = ""
    var ikinciCumle: String = ""
    var gorselUrl: String? = null

    var docId: String = ""
    var dogruSayisi: Int = 0
    var sonDogruMs: Long = 0L

    // Boş constructor — Firebase için gerekli
    constructor()

    // Dolu constructor — elle oluşturmak için kolaylık
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
