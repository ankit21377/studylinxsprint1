package com.example.studylinx.repo



import com.example.studylinx.model.Event
import com.example.studylinx.model.HomeSummary
import com.example.studylinx.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HomeRepoImpl(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : HomeRepo {

    private fun uid(): String = auth.currentUser?.uid ?: "demo_user"

    override fun observeCountries(): Flow<List<Country>> = callbackFlow {
        val reg = db.collection("countries")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    Country(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        flagUrl = d.getString("flagUrl") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override fun observeUniversities(limit: Int): Flow<List<University>> = callbackFlow {
        val reg = db.collection("universities")
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    University(
                        id = d.id,
                        name = d.getString("name") ?: "",
                        city = d.getString("city") ?: "",
                        country = d.getString("country") ?: "",
                        description = d.getString("description") ?: "",
                        imageUrl = d.getString("imageUrl") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    override fun observeUpcomingAppointment(): Flow<Appointment?> = callbackFlow {
        val reg = db.collection("users").document(uid())
            .collection("appointments")
            .orderBy("dateTime")
            .limit(1)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val doc = snap?.documents?.firstOrNull()
                if (doc == null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val ts = doc.getTimestamp("dateTime")
                val millis = ts?.toDate()?.time ?: 0L

                trySend(
                    Appointment(
                        id = doc.id,
                        counselorName = doc.getString("counselorName") ?: "",
                        status = doc.getString("status") ?: "Pending",
                        dateTimeMillis = millis
                    )
                )
            }
        awaitClose { reg.remove() }
    }

    override fun observeProgress(): Flow<ApplicationProgress> = callbackFlow {
        val reg = db.collection("users").document(uid())
            .collection("progress").document("main")
            .addSnapshotListener { doc, err ->
                if (err != null) {
                    trySend(ApplicationProgress())
                    return@addSnapshotListener
                }
                val currentStep = (doc?.getLong("currentStep") ?: 0L).toInt()

                val steps = doc?.get("steps") as? Map<*, *> ?: emptyMap<String, Any>()
                val submitted = steps["submitted"] as? Boolean ?: false
                val inReview = steps["inReview"] as? Boolean ?: false
                val interview = steps["interview"] as? Boolean ?: false
                val finalDecision = steps["finalDecision"] as? Boolean ?: false

                trySend(
                    ApplicationProgress(
                        currentStep = currentStep,
                        submitted = submitted,
                        inReview = inReview,
                        interview = interview,
                        finalDecision = finalDecision
                    )
                )
            }
        awaitClose { reg.remove() }
    }

    override suspend fun updateProgress(stepIndex: Int, completed: Boolean) {
        val stepKey = when (stepIndex) {
            0 -> "submitted"
            1 -> "inReview"
            2 -> "interview"
            else -> "finalDecision"
        }

        val progressDoc = db.collection("users").document(uid())
            .collection("progress").document("main")

        // update boolean
        progressDoc.set(
            mapOf("steps" to mapOf(stepKey to completed)),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()

        // recompute currentStep (simple rule: highest completed consecutive)
        val snapshot = progressDoc.get().await()
        val steps = snapshot.get("steps") as? Map<*, *> ?: emptyMap<String, Any>()
        val s0 = steps["submitted"] as? Boolean ?: false
        val s1 = steps["inReview"] as? Boolean ?: false
        val s2 = steps["interview"] as? Boolean ?: false
        val s3 = steps["finalDecision"] as? Boolean ?: false

        val newStep = when {
            s3 -> 3
            s2 -> 2
            s1 -> 1
            s0 -> 0
            else -> 0
        }

        progressDoc.set(mapOf("currentStep" to newStep), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    override suspend fun seedIfEmpty() {
        // Seed countries if none
        val countriesSnap = db.collection("countries").limit(1).get().await()
        if (countriesSnap.isEmpty) {
            val batch = db.batch()
            fun add(id: String, name: String, flagUrl: String) {
                batch.set(db.collection("countries").document(id), mapOf("name" to name, "flagUrl" to flagUrl))
            }
            add("canada", "Canada", "https://flagcdn.com/w1280/ca.png")
            add("usa", "USA", "https://flagcdn.com/w1280/us.png")
            add("australia", "Australia", "https://flagcdn.com/w1280/au.png")
            add("uk", "United Kingdom", "https://flagcdn.com/w1280/gb.png")
            add("japan", "Japan", "https://flagcdn.com/w1280/jp.png")
            batch.commit().await()
        }

        // Seed progress doc if missing
        val progressDoc = db.collection("users").document(uid()).collection("progress").document("main")
        val progSnap = progressDoc.get().await()
        if (!progSnap.exists()) {
            progressDoc.set(
                mapOf(
                    "currentStep" to 0,
                    "steps" to mapOf(
                        "submitted" to false,
                        "inReview" to false,
                        "interview" to false,
                        "finalDecision" to false
                    )
                )
            ).await()
        }

        // Seed appointment if none (optional)
        val apptSnap = db.collection("users").document(uid()).collection("appointments").limit(1).get().await()
        if (apptSnap.isEmpty) {
            db.collection("users").document(uid()).collection("appointments").add(
                mapOf(
                    "counselorName" to "Mr. Smith",
                    "status" to "Confirmed",
                    "dateTime" to Timestamp.now()
                )
            ).await()
        }
    }
}