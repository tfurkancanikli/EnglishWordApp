package com.anlarsinsoftware.englishwordsapp.Model

data class Kelime(
    val kelimeId: String = "",
    val kullaniciAdi: String = "",
    val kelimeIng: String = "",
    val kelimeTur: String = "",
    val birinciCumle: String = "",
    val ikinciCumle: String = "",
    val gorselUrl: String = ""
) {

    var docId: String = ""
    var dogruSayisi: Int = 0
    var sonDogruMs: Long = 0L
}
