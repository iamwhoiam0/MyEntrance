package com.example.myentrance.domain.entities

data class ChatMessageWithProfile(
    val id: String = "",
    val senderId: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val senderName: String = "",
    val senderAvatarUrl: String? = null
) {
    fun getFormattedTime(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))
    }

    fun getFormattedDate(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val today = java.util.Calendar.getInstance()
        val yesterday = java.util.Calendar.getInstance()
        yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)

        return when {
            isSameDay(calendar, today) -> "Сегодня"
            isSameDay(calendar, yesterday) -> "Вчера"
            else -> {
                val sdf = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru"))
                sdf.format(calendar.time)
            }
        }
    }

    private fun isSameDay(cal1: java.util.Calendar, cal2: java.util.Calendar): Boolean {
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
