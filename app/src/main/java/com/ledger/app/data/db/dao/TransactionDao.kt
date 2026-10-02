package com.ledger.app.data.db.dao

import androidx.room.*
import com.ledger.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions
        ORDER BY dateEpochDay DESC, timeMinuteOfDay DESC
    """)
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
        ORDER BY dateEpochDay DESC, timeMinuteOfDay DESC
    """)
    fun getInRange(fromDay: Long, toDay: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE (accountId = :accountId OR toAccountId = :accountId)
        ORDER BY dateEpochDay DESC, timeMinuteOfDay DESC
    """)
    fun getByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE categoryId = :categoryId
        ORDER BY dateEpochDay DESC, timeMinuteOfDay DESC
    """)
    fun getByCategory(categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("""
        SELECT COALESCE(SUM(ABS(amount)), 0.0) FROM transactions
        WHERE type = 'EXPENSE' AND dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
    """)
    suspend fun getTotalExpense(fromDay: Long, toDay: Long): Double

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'INCOME' AND dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
    """)
    suspend fun getTotalIncome(fromDay: Long, toDay: Long): Double

    @Query("""
        SELECT categoryId, COALESCE(SUM(ABS(amount)), 0.0) as total, COUNT(*) as opsCount
        FROM transactions
        WHERE type = 'EXPENSE' AND dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
        GROUP BY categoryId
    """)
    suspend fun getExpenseByCategory(fromDay: Long, toDay: Long): List<CategoryTotal>

    @Query("""
        SELECT dateEpochDay, COALESCE(SUM(ABS(amount)), 0.0) as total
        FROM transactions
        WHERE type = 'EXPENSE' AND dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
        GROUP BY dateEpochDay
        ORDER BY dateEpochDay ASC
    """)
    suspend fun getDailyExpense(fromDay: Long, toDay: Long): List<DailyTotal>

    @Query("""
        SELECT categoryId, COALESCE(SUM(amount), 0.0) as total, COUNT(*) as opsCount
        FROM transactions
        WHERE type = 'INCOME' AND dateEpochDay >= :fromDay AND dateEpochDay <= :toDay
        GROUP BY categoryId
    """)
    suspend fun getIncomeByCategory(fromDay: Long, toDay: Long): List<CategoryTotal>
}

data class CategoryTotal(val categoryId: String, val total: Double, val opsCount: Int)
data class DailyTotal(val dateEpochDay: Long, val total: Double)
