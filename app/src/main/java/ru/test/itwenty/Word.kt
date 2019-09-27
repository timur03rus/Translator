package ru.test.itwenty

import java.util.*

class Word internal constructor(var word: String?, var translation: String?, sourcePosition: Int, targetPosition: Int) {
    var sourcePosition = -1
    var targetPosition = -1
    var sourceLanguage: String? = null
        private set
    var targetLanguage: String? = null
        private set

    val isEmpty: Boolean
        get() = word == null && targetPosition == -1 && sourcePosition == -1

    init {
        this.sourcePosition = sourcePosition
        this.targetPosition = targetPosition
        if (Locale.getDefault().language == "en") {
            sourceLanguage = Languages.getLangCodeEN(sourcePosition).toUpperCase()
            targetLanguage = Languages.getLangCodeEN(targetPosition).toUpperCase()
        } else {
            sourceLanguage = Languages.getLangCodeRU(sourcePosition).toUpperCase()
            targetLanguage = Languages.getLangCodeRU(targetPosition).toUpperCase()
        }
    }
}
