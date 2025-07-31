package com.example.myentrance.data.repository

import com.example.myentrance.domain.entities.Attachment
import com.example.myentrance.domain.entities.AttachmentType
import com.example.myentrance.domain.repository.AttachmentRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AttachmentRepositoryImpl(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : AttachmentRepository {

    override suspend fun uploadAttachment(attachment: Attachment): String {
        val fileRef = storage.reference.child("attachments/${attachment.id}_${attachment.name}")
        // Для примера: attachment.url содержит путь к локальному файлу!
        val stream = java.io.File(attachment.url).inputStream()
        fileRef.putStream(stream).await()
        return fileRef.downloadUrl.await().toString()
    }

    override suspend fun deleteAttachment(attachmentId: String): Boolean {
        storage.reference.child("attachments/$attachmentId").delete().await()
        return true
    }

    override suspend fun getAttachments(ids: List<String>): List<Attachment> {
        // Пример: получение ссылок (этот метод требует Firestore+Storage)
        return ids.map { id ->
            val url = storage.reference.child("attachments/$id").downloadUrl.await().toString()
            Attachment(
                id = id,
                url = url,
                type = AttachmentType.IMAGE,
                name = id // или другое логичное имя
            )
        }
    }
}