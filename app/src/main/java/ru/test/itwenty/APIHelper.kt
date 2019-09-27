package ru.test.itwenty

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface APIHelper {
    @POST("api/v1.5/tr.json/translate")
    fun getTranslation(@Query("key") APIKey: String,
                       @Query("text") textToTranslate: String,
                       @Query("lang") lang: String): Call<TranslatedText>
}
