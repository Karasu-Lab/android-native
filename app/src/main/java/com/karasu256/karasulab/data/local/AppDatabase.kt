package com.karasu256.karasulab.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

@Database(
    entities = [AuthTokenEntity::class, AccountEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = AppDatabase.DropUserProfile::class)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authTokenDao(): AuthTokenDao

    abstract fun accountDao(): AccountDao

    @DeleteTable(tableName = "user_profile")
    class DropUserProfile : AutoMigrationSpec

    companion object {
        const val NAME = "app.db"
    }
}
