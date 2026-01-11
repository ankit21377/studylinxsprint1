package com.example.studylinx.repo

import com.example.studylinx.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AppointmentRepoImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AppointmentRepo {

    private fun col(userId: String) =
        db.collection("users").document(userId).collection("appointments")

    override fun streamAppointmentsInRange(
        userId: String,
        startMillisInclusive: Long,
        endMillisExclusive: Long
    ): Flow<List<Appointment>> = callbackFlow {
        val reg = col(userId)
            .whereGreaterThanOrEqualTo("startMillis", startMillisInclusive)
            .whereLessThan("startMillis", endMillisExclusive)
            .orderBy("startMillis", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { d ->
                    Appointment(
                        id = d.id,
                        title = d.getString("title") ?: "",
                        note = d.getString("note") ?: "",
                        startMillis = d.getLong("startMillis") ?: 0L,
                        endMillis = d.getLong("endMillis") ?: 0L,
                        createdAt = d.getLong("createdAt") ?: 0L,
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }.orEmpty()

                trySend(list)
            }

        awaitClose { reg.remove() }
    }

    override suspend fun addAppointment(userId: String, appointment: Appointment): String {
        val now = System.currentTimeMillis()
        val data = hashMapOf(
            "title" to appointment.title,
            "note" to appointment.note,
            "startMillis" to appointment.startMillis,
            "endMillis" to appointment.endMillis,
            "createdAt" to now,
            "updatedAt" to now
        )
        return col(userId).add(data).await().id
    }
}