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
import com.healthtech.doccareplusadmin.domain.model.User
import com.healthtech.doccareplusadmin.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {
    private val TAG = "UserApi"

    fun getUsers(): Flow<List<User>> = callbackFlow {
        val userRef = database.getReference("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.tag(TAG).d("snapshot: $snapshot")
                val userList = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    Timber.tag(TAG).d("User: ${userSnapshot.value}")
                    userSnapshot.getValue(User::class.java)?.let { userItem ->
                        userList.add(userItem)
                    }
                }
                trySend(userList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Error getUsers: ${error.message}")
            }
        }
        userRef.addValueEventListener(listener)
        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    suspend fun getUserById(userId: String): Result<User?> {
        val userRef = database.getReference("users")
        return try {
            val snapshot = userRef.child(userId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }
            val user = snapshot.getValue(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addUser(user: User): Result<Unit> {
        try {
            val userRef = database.getReference("users")
            val snapshot = userRef.orderByChild("email").equalTo(user.email).get().await()
            if (snapshot.exists()) {
                return Result.failure(Exception("User already exists in database"))
            }

            val authResult = try {
                auth.createUserWithEmailAndPassword(user.email, Constants.DEFAULT_USER_PASSWORD)
                    .await()
            } catch (e: FirebaseAuthUserCollisionException) {
                return Result.failure(Exception("Email already registered"))
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                return Result.failure(Exception("Invalid email format"))
            } catch (e: FirebaseAuthWeakPasswordException) {
                return Result.failure(Exception("Password too weak"))
            }

            val userId =
                authResult.user?.uid ?: return Result.failure(Exception("User creation failed"))

            authResult.user?.let { firebaseUser ->
                firebaseUser.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(user.name)
                        .build()
                ).await()

                firebaseUser.sendEmailVerification().await()
                auth.currentUser?.reload()?.await()
                firebaseUser.verifyBeforeUpdateEmail(user.email).await()
            }

            val finalUser = user.copy(id = userId)
            userRef.child(userId).setValue(finalUser).await()

            return Result.success(Unit)
        } catch (e: Exception) {
            try {
                auth.currentUser?.delete()?.await()
            } catch (deleteError: Exception) {
                Timber.e(deleteError, "Failed to delete auth user during cleanup")
            }
            return Result.failure(e)
        }
//        val authResult =
//            auth.createUserWithEmailAndPassword(user.email, Constants.DEFAULT_USER_PASSWORD).await()
//        val userId =
//            authResult.user?.uid ?: return Result.failure(Exception("User creation failed"))
//        val finalUser = user.copy(id = userId)
//        val userRef = database.getReference("users")
//        return try {
//            val snapshot = userRef.child(userId).get().await()
//            if (snapshot.exists()) {
//                return Result.failure(Exception("User already exists"))
//            }
//            userRef.child(userId).setValue(finalUser).await()
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        val userRef = database.getReference("users")
        return try {
            val snapshot = userRef.child(user.id).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }
            userRef.child(user.id).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        val userRef = database.getReference("users")
        return try {
            val snapshot = userRef.child(userId).get().await()
            if (!snapshot.exists()) {
                return Result.failure(Exception("User not found"))
            }
            userRef.child(userId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}