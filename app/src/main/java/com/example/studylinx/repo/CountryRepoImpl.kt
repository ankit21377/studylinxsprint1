package com.example.studylinx.repo

import com.example.studylinx.model.Country
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CountryRepoImpl(
    private val db: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child("countries")
) : CountryRepo {

    override fun observeCountries(): Flow<List<Country>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { snap ->
                    // id is the key
                    val c = snap.getValue(Country::class.java) ?: return@mapNotNull null
                    c.copy(id = snap.key ?: "")
                }
                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.addValueEventListener(listener)
        awaitClose { db.removeEventListener(listener) }
    }

    override suspend fun addCountry(country: Country) {
        suspendCancellableCoroutine<Unit> { cont ->
            val key = db.push().key ?: run {
                cont.resumeWithException(IllegalStateException("Failed to generate key"))
                return@suspendCancellableCoroutine
            }
            val data = country.copy(id = key)
            db.child(key).setValue(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    override suspend fun upsertCountry(country: Country) {
        suspendCancellableCoroutine<Unit> { cont ->
            val id = country.id.trim()
            val key = if (id.isNotBlank()) id else (db.push().key ?: "")
            if (key.isBlank()) {
                cont.resumeWithException(IllegalStateException("Failed to generate key"))
                return@suspendCancellableCoroutine
            }
            val data = country.copy(id = key)
            db.child(key).setValue(data)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    override suspend fun deleteCountry(countryId: String) {
        suspendCancellableCoroutine<Unit> { cont ->
            db.child(countryId).removeValue()
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }
}