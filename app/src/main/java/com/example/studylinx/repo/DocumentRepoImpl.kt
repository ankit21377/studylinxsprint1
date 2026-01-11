package com.example.studylinx.repo

import android.net.Uri
import com.example.studylinx.model.UserDocument
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DocumentRepoImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : DocumentRepo{

    override suspend fun uploadDocument(
        userId: String,
        docType: String,
        fileUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit
    ): UserDocument {
        val ts = System.currentTimeMillis()
        val safeName = fileName.replace(Regex("""[^\w\-. ]"""), "_")
        val storagePath = "documents/$userId/$docType/${ts}_$safeName"
        val ref = storage.reference.child(storagePath)

        val task = ref.putFile(fileUri)
        task.addOnProgressListener { snap ->
            val total = snap.totalByteCount.coerceAtLeast(1L)
            val percent = ((snap.bytesTransferred * 100) / total).toInt()
            onProgress(percent.coerceIn(0, 100))
        }
        task.await()

        val url = ref.downloadUrl.await().toString()

        val doc = UserDocument(
            docType = docType,
            fileName = fileName,
            fileUrl = url,
            storagePath = storagePath,
            uploadedAt = ts,
            updatedAt = ts
        )

        // users/{userId}/documents/{docType}
        firestore.collection("users")
            .document(userId)
            .collection("documents")
            .document(docType)
            .set(doc, SetOptions.merge())
            .await()

        return doc
    }

    override fun observeUserDocuments(userId: String): Flow<Map<String, UserDocument>> = callbackFlow {
        val reg = firestore.collection("users")
            .document(userId)
            .collection("documents")
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val map = mutableMapOf<String, UserDocument>()
                snap?.documents?.forEach { d ->
                    val docType = d.id
                    val item = d.toObject(UserDocument::class.java)
                    if (item != null) map[docType] = item.copy(docType = docType)
                }
                trySend(map).isSuccess
            }

        awaitClose { reg.remove() }
    }
}
