package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Doctor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorApi @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val TAG = "DoctorApi"

    fun getDoctors(): Flow<List<Doctor>> = callbackFlow {
        val doctorRef = database.getReference("doctors")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorList = mutableListOf<Doctor>()
                for (doctorSnapshot in snapshot.children) {
                    doctorSnapshot.getValue(Doctor::class.java)?.let { doctor ->
                        doctorList.add(doctor)
                    }
                }
                trySend(doctorList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getDoctors: ${error.message}")
            }
        }
        doctorRef.addValueEventListener(listener)
        awaitClose { doctorRef.removeEventListener(listener) }
    }

    suspend fun getDoctorById(doctorId: String): Result<Doctor?> {
        val doctorRef = database.getReference("doctors")
        return try {
            val snapshot = doctorRef.child(doctorId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }
            val doctor = snapshot.getValue(Doctor::class.java)
            Result.success(doctor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDoctor(doctor: Doctor): Result<Unit> {
        val doctorRef = database.getReference("doctors")
        return try {
            val snapshot = doctorRef.child(doctor.id).get().await()
            if (snapshot.exists()) {
                return Result.failure(Exception("Doctor already exists"))
            }
            doctorRef.child(doctor.id).setValue(doctor).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctor: Doctor): Result<Unit> {
        val doctorRef = database.getReference("doctors")
        return try {
            val snapshot = doctorRef.child(doctor.id).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }
            doctorRef.child(doctor.id).setValue(doctor).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(doctorId: String): Result<Unit> {
        val doctorRef = database.getReference("doctors")
        return try {
            val snapshot = doctorRef.child(doctorId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("Doctor not found"))
            }
            doctorRef.child(doctorId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}