package com.example.myentrance.utils

import android.text.Editable
import android.text.TextWatcher

class PhoneNumberFormattingTextWatcher : TextWatcher {
    private var isFormatting = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting || s == null) return
        isFormatting = true

        // оставляем только цифры
        val digits = s.toString().replace(Regex("[^\\d]"), "")
            .take(10) // максимум 10 цифр для РФ без кода страны

        val formatted = buildString {
            for (i in digits.indices) {
                when (i) {
                    0 -> append("(")
                    3 -> append(") ")
                    6 -> append("-")
                    8 -> append("-")
                }
                append(digits[i])
            }
        }

        s.replace(0, s.length, formatted)
        isFormatting = false
    }
}

