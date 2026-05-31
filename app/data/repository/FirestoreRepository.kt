package com.budgetwise.app.data.repository

import android.util.Log
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.data.local.entity.MonthlyGoal
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreRepository"

/**
 * FirestoreRepository — syncs BudgetWise data to Firebase Firestore.
 *
 * Firestore structure:
 *   users/{userId}/expenses/{expenseId}   — expense records
 *   users/{userId}/goals/{month_year}     — monthly goals
 *
 * This repository is intentionally simple (fire-and-forget writes) so that
 * the local Room database remains the source of truth and the app works
 * fully offline. Firestore acts as a backup / multi-device sync layer.
 *
 * Called from ExpenseViewModel.saveExpense() and GoalsViewModel.saveGoal()
 * after the local Room insert succeeds.
 *
 * Own Feature 1: Online data backup — every expense and goal is mirrored
 * to Firestore so the user's data is accessible across devices.
 */
@Singleton
class FirestoreRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()

    // ── Expenses ──────────────────────────────────────────────────────────────

    /**
     * Write a single expense to Firestore.
     * Document ID = expense.id (Room auto-generated Long, converted to String).
     * Silently logs on failure — local Room data is unaffected.
     *
     * @param userId  The logged-in user's Room id (used as Firestore user doc id).
     * @param expense The expense entity to sync.
     */
    suspend fun saveExpense(userId: Long, expense: Expense) {
        try {
            val data = mapOf(
                "id"          to expense.id,
                "userId"      to expense.userId,
                "categoryId"  to expense.categoryId,
                "amount"      to expense.amount,
                "description" to expense.description,
                "date"        to expense.date,
                "startTime"   to expense.startTime,
                "endTime"     to expense.endTime,
                "photoUri"    to expense.photoUri,
                "createdAt"   to expense.createdAt,
                "syncedAt"    to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId.toString())
                .collection("expenses")
                .document(expense.id.toString())
                .set(data)
                .await()
            Log.d(TAG, "Expense ${expense.id} synced to Firestore for user $userId")
        } catch (e: Exception) {
            // Non-fatal — local data already saved to Room
            Log.w(TAG, "Failed to sync expense ${expense.id} to Firestore: ${e.message}")
        }
    }

    /**
     * Delete an expense from Firestore by id.
     * Called after Room deletion succeeds.
     */
    suspend fun deleteExpense(userId: Long, expenseId: Long) {
        try {
            db.collection("users")
                .document(userId.toString())
                .collection("expenses")
                .document(expenseId.toString())
                .delete()
                .await()
            Log.d(TAG, "Expense $expenseId deleted from Firestore for user $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete expense $expenseId from Firestore: ${e.message}")
        }
    }

    // ── Monthly Goals ─────────────────────────────────────────────────────────

    /**
     * Write a monthly goal to Firestore.
     * Document ID = "month_year" e.g. "5_2026".
     */
    suspend fun saveGoal(userId: Long, goal: MonthlyGoal) {
        try {
            val docId = "${goal.month}_${goal.year}"
            val data  = mapOf(
                "userId"    to goal.userId,
                "month"     to goal.month,
                "year"      to goal.year,
                "minGoal"   to goal.minGoal,
                "maxGoal"   to goal.maxGoal,
                "updatedAt" to goal.updatedAt,
                "syncedAt"  to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId.toString())
                .collection("goals")
                .document(docId)
                .set(data)
                .await()
            Log.d(TAG, "Goal $docId synced to Firestore for user $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync goal to Firestore: ${e.message}")
        }
    }

    // ── Streak data ───────────────────────────────────────────────────────────

    /**
     * Write the user's current streak count to Firestore.
     * Own Feature 2: Spending Streak — tracks consecutive days the user
     * logged at least one expense. Synced online so streak survives reinstalls.
     */
    suspend fun saveStreak(userId: Long, streakDays: Int, lastLogDate: Long) {
        try {
            val data = mapOf(
                "streakDays"  to streakDays,
                "lastLogDate" to lastLogDate,
                "updatedAt"   to System.currentTimeMillis()
            )
            db.collection("users")
                .document(userId.toString())
                .collection("streak")
                .document("current")
                .set(data)
                .await()
            Log.d(TAG, "Streak $streakDays days synced for user $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync streak: ${e.message}")
        }
    }
}