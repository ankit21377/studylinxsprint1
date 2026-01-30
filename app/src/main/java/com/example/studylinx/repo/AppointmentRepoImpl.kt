// File: com/example/studylinx/repo/AppointmentRepoImpl.kt
package com.example.studylinx.repo

import com.example.studylinx.model.Appointment
import com.google.firebase.database.*

class AppointmentRepoImpl : AppointmentRepo {

    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    private var activeRef: DatabaseReference? = null
    private var activeListener: ValueEventListener? = null

    private fun userAppointmentsRef(userId: String): DatabaseReference {
        // Path: users/{uid}/appointments/{appointmentId}
        return db.child("users").child(userId).child("appointments")
    }

    override fun observeAppointments(
        userId: String,
        onUpdate: (List<Appointment>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId.isBlank()) {
            onError("UserId is blank")
            return
        }

        // stop old
        stop()

        val ref = userAppointmentsRef(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Appointment>()
                for (child in snapshot.children) {
                    val ap = child.getValue(Appointment::class.java)
                    if (ap != null) {
                        val fixed = if (ap.id.isBlank()) ap.copy(id = child.key ?: "") else ap
                        list.add(fixed)
                    }
                }
                onUpdate(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }

        activeRef = ref
        activeListener = listener
        ref.addValueEventListener(listener)
    }

    override fun addAppointment(
        userId: String,
        appointment: Appointment,
        onDone: (Boolean, String?) -> Unit
    ) {
        if (userId.isBlank()) {
            onDone(false, "UserId is blank")
            return
        }

        val ref = userAppointmentsRef(userId)
        val newRef = ref.push()
        val id = newRef.key ?: run {
            onDone(false, "Could not generate appointment id")
            return
        }

        val toSave = appointment.copy(
            id = id,
            userId = userId
        )

        newRef.setValue(toSave)
            .addOnSuccessListener { onDone(true, "Appointment added") }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Failed to add") }
    }

    override fun stop() {
        val r = activeRef
        val l = activeListener
        if (r != null && l != null) {
            r.removeEventListener(l)
        }
        activeRef = null
        activeListener = null
    }
}
