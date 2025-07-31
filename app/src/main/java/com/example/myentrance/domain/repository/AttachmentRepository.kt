package com.example.myentrance.domain.repository

import com.example.myentrance.domain.entities.Attachment

interface AttachmentRepository {
    suspend fun uploadAttachment(attachment: Attachment): String // Возвращает URL
    suspend fun deleteAttachment(attachmentId: String): Boolean
    suspend fun getAttachments(ids: List<String>): List<Attachment>
}
