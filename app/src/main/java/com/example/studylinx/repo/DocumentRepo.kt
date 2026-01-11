package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.UserDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepo {
    suspend fun uploadDocument(
        userId: String,
        docType: String,
        fileUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit
    ): UserDocument

    fun observeUserDocuments(userId: String): Flow<Map<String, UserDocument>>
}