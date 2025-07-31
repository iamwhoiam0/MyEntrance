package com.example.myentrance.domain.entities

data class Attachment(
    val id: String,
    val url: String,
    val type: AttachmentType,
    val name: String
)
