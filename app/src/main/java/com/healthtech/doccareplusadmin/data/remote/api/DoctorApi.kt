package com.healthtech.doccareplusadmin.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.healthtech.doccareplusadmin.domain.model.Doctor
import com.healthtech.doccareplusadmin.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoctorApi @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {
    private val TAG = "DoctorApi"
    private val doctorRef = database.getReference("doctors")

    fun getDoctors(): Flow<List<Doctor>> = callbackFlow {
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
        return try {
            val snapshot = doctorRef.orderByChild("email").equalTo(doctor.email).get().await()
            if (snapshot.exists()) {
                return Result.failure(Exception("Doctor already exists with this email"))
            }
            val authResult = try {
                auth.createUserWithEmailAndPassword(doctor.email, Constants.DEFAULT_USER_PASSWORD)
                    .await()
            } catch (e: FirebaseAuthUserCollisionException) {
                return Result.failure(Exception("Email already registered"))
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                return Result.failure(Exception("Invalid email format"))
            } catch (e: FirebaseAuthWeakPasswordException) {
                return Result.failure(Exception("Password too weak"))
            }

            val doctorId =
                authResult.user?.uid ?: return Result.failure(Exception("Doctor creation failed"))
            authResult.user?.let { firebaseUser ->
                firebaseUser.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(doctor.name).build()
                ).await()

                firebaseUser.sendEmailVerification().await()
                auth.currentUser?.reload()?.await()
                firebaseUser.verifyBeforeUpdateEmail(doctor.email).await()
            }

            val finalDoctor = doctor.copy(id = doctorId)
            doctorRef.child(doctorId).setValue(finalDoctor).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDoctor(doctor: Doctor): Result<Unit> {
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