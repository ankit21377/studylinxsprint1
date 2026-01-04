package com.example.studylinx.data.repo


import com.example.studylinx.model.Event
import com.example.studylinx.model.HomeSummary
import com.google.firebase.firestore.FirebaseFirestore

class HomeRepoImpl : HomeRepo {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ✅ Recommended structure: users/{uid}/homeData/home
    private fun homeDoc(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("homeData")
            .document("home")

    // ✅ Events: users/{uid}/events/{eventId}
    private fun eventsCol(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("events")

    override fun getHomeSummary(
        userId: String,
        callback: (Boolean, String, HomeSummary?) -> Unit
    ) {
        homeDoc(userId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    callback(true, "No home summary found (empty)", HomeSummary())
                    return@addOnSuccessListener
                }
                val summary = snap.toObject(HomeSummary::class.java) ?: HomeSummary()
                callback(true, "Home summary fetched", summary)
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to fetch home summary", null)
            }
    }

    override fun saveHomeSummary(
        userId: String,
        summary: HomeSummary,
        callback: (Boolean, String) -> Unit
    ) {
        homeDoc(userId)
            .set(summary)
            .addOnSuccessListener {
                callback(true, "Home summary saved")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to save home summary")
            }
    }

    override fun getEvents(
        userId: String,
        callback: (Boolean, String, List<Event>?) -> Unit
    ) {
        eventsCol(userId)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { doc ->
                    val e = doc.toObject(Event::class.java) ?: Event()
                    // ✅ ensure id is set from document id
                    e.copy(id = doc.id)
                }
                callback(true, "Events fetched", list)
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to fetch events", null)
            }
    }

    override fun addEvent(
        userId: String,
        event: Event,
        callback: (Boolean, String) -> Unit
    ) {
        val docRef = if (event.id.isNotBlank()) {
            eventsCol(userId).document(event.id)
        } else {
            eventsCol(userId).document()
        }

        val toSave = event.copy(id = docRef.id)

        docRef.set(toSave)
            .addOnSuccessListener {
                callback(true, "Event added")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to add event")
            }
    }

    override fun deleteEvent(
        userId: String,
        eventId: String,
        callback: (Boolean, String) -> Unit
    ) {
        eventsCol(userId)
            .document(eventId)
            .delete()
            .addOnSuccessListener {
                callback(true, "Event deleted")
            }
            .addOnFailureListener { e ->
                callback(false, e.message ?: "Failed to delete event")
            }
    }
}