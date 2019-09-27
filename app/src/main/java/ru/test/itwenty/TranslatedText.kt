package ru.test.itwenty

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TranslatedText {

    @SerializedName("code")
    @Expose
    var code: Int? = null
    @SerializedName("lang")
    @Expose
    var lang: String? = null
    @SerializedName("text")
    @Expose
    var text: List<String>? = null

}
