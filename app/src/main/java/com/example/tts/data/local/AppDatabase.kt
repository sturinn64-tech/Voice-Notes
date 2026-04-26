package com.example.tts.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AudioMessageEntity::class,
        FolderEntity::class,
        TagEntity::class,
        AudioMessageTagCrossRef::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun audioMessageDao(): AudioMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "transcriptionStatus",
                    columnSql = "TEXT NOT NULL DEFAULT 'COMPLETED'"
                )

                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "transcriptionError",
                    columnSql = "TEXT"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "folderName",
                    columnSql = "TEXT NOT NULL DEFAULT ''"
                )

                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "tags",
                    columnSql = "TEXT NOT NULL DEFAULT '[]'"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "folderName",
                    columnSql = "TEXT NOT NULL DEFAULT ''"
                )

                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "tags",
                    columnSql = "TEXT NOT NULL DEFAULT '[]'"
                )

                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "isDeleted",
                    columnSql = "INTEGER NOT NULL DEFAULT 0"
                )

                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "deletedAt",
                    columnSql = "INTEGER"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing(
                    tableName = "audio_messages",
                    columnName = "folderId",
                    columnSql = "INTEGER"
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS folders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_folders_userId_name
                    ON folders(userId, name)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_folders_userId
                    ON folders(userId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_tags_userId_name
                    ON tags(userId, name)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_tags_userId
                    ON tags(userId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS audio_message_tag_refs (
                        messageId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        PRIMARY KEY(messageId, tagId),
                        FOREIGN KEY(messageId) REFERENCES audio_messages(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_audio_message_tag_refs_messageId
                    ON audio_message_tag_refs(messageId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_audio_message_tag_refs_tagId
                    ON audio_message_tag_refs(tagId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_audio_messages_userId_folderId
                    ON audio_messages(userId, folderId)
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_notes.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

private fun SupportSQLiteDatabase.addColumnIfMissing(
    tableName: String,
    columnName: String,
    columnSql: String
) {
    if (!hasColumn(tableName, columnName)) {
        execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnSql")
    }
}

private fun SupportSQLiteDatabase.hasColumn(
    tableName: String,
    columnName: String
): Boolean {
    val cursor = query("PRAGMA table_info($tableName)")
    return cursor.use {
        val nameIndex = it.getColumnIndex("name")
        while (it.moveToNext()) {
            if (nameIndex >= 0 && it.getString(nameIndex) == columnName) {
                return@use true
            }
        }
        false
    }
}