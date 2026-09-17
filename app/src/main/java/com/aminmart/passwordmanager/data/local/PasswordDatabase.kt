package com.aminmart.passwordmanager.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Simple SQLite database helper for password manager.
 */
class PasswordDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val database: SQLiteDatabase = writableDatabase

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_PASSWORDS_TABLE)
        db.execSQL(CREATE_SETTINGS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS passwords")
        db.execSQL("DROP TABLE IF EXISTS settings")
        onCreate(db)
    }

    // Password operations
    fun getAllPasswords(): Flow<List<PasswordEntity>> = flow {
        val list = mutableListOf<PasswordEntity>()
        val cursor = database.rawQuery("SELECT * FROM passwords ORDER BY updatedAt DESC", null)
        while (cursor.moveToNext()) {
            list.add(cursorToPasswordEntity(cursor))
        }
        cursor.close()
        emit(list)
    }

    suspend fun getAllPasswordsList(): List<PasswordEntity> {
        val list = mutableListOf<PasswordEntity>()
        val cursor = database.rawQuery("SELECT * FROM passwords ORDER BY updatedAt DESC", null)
        while (cursor.moveToNext()) {
            list.add(cursorToPasswordEntity(cursor))
        }
        cursor.close()
        return list
    }

    suspend fun getPasswordById(id: Long): PasswordEntity? {
        val cursor = database.query(
            TABLE_PASSWORDS,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var entity: PasswordEntity? = null
        if (cursor.moveToFirst()) {
            entity = cursorToPasswordEntity(cursor)
        }
        cursor.close()
        return entity
    }

    suspend fun insertPassword(password: PasswordEntity): Long {
        val values = ContentValues().apply {
            put("title", password.title)
            put("username", password.username)
            put("password_encrypted", password.passwordEncrypted)
            put("website", password.website)
            put("notes_encrypted", password.notesEncrypted)
            put("category", password.category.name)
            put("icon", password.icon)
            put("ciphertext", password.ciphertext)
            put("nonce", password.nonce)
            put("createdAt", password.createdAt)
            put("updatedAt", password.updatedAt)
        }
        return database.insert(TABLE_PASSWORDS, null, values)
    }

    suspend fun updatePassword(password: PasswordEntity) {
        val values = ContentValues().apply {
            put("title", password.title)
            put("username", password.username)
            put("password_encrypted", password.passwordEncrypted)
            put("website", password.website)
            put("notes_encrypted", password.notesEncrypted)
            put("category", password.category.name)
            put("icon", password.icon)
            put("ciphertext", password.ciphertext)
            put("nonce", password.nonce)
            put("updatedAt", password.updatedAt)
        }
        database.update(TABLE_PASSWORDS, values, "id = ?", arrayOf(password.id.toString()))
    }

    suspend fun deletePassword(password: PasswordEntity) {
        database.delete(TABLE_PASSWORDS, "id = ?", arrayOf(password.id.toString()))
    }

    suspend fun deletePasswordById(id: Long) {
        database.delete(TABLE_PASSWORDS, "id = ?", arrayOf(id.toString()))
    }

    // Settings operations
    suspend fun getSetting(key: String): SettingsEntity? {
        val cursor = database.query(
            TABLE_SETTINGS,
            null,
            "key = ?",
            arrayOf(key),
            null, null, null
        )
        var entity: SettingsEntity? = null
        if (cursor.moveToFirst()) {
            entity = cursorToSettingsEntity(cursor)
        }
        cursor.close()
        return entity
    }

    fun getSettingFlow(key: String): Flow<SettingsEntity?> = flow {
        emit(getSetting(key))
    }

    suspend fun saveSetting(setting: SettingsEntity) {
        val values = ContentValues().apply {
            put("key", setting.key)
            put("value", setting.value)
        }
        database.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    suspend fun deleteSetting(key: String) {
        database.delete(TABLE_SETTINGS, "key = ?", arrayOf(key))
    }

    suspend fun hasSetting(key: String): Boolean {
        val cursor = database.rawQuery(
            "SELECT EXISTS(SELECT 1 FROM settings WHERE key = ?)",
            arrayOf(key)
        )
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) == 1
        }
        cursor.close()
        return exists
    }

    private fun cursorToPasswordEntity(cursor: Cursor): PasswordEntity {
        return PasswordEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
            passwordEncrypted = cursor.getString(cursor.getColumnIndexOrThrow("password_encrypted")),
            website = cursor.getString(cursor.getColumnIndexOrThrow("website")),
            notesEncrypted = cursor.getString(cursor.getColumnIndexOrThrow("notes_encrypted")),
            category = PasswordCategory.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("category"))),
            icon = cursor.getString(cursor.getColumnIndexOrThrow("icon")),
            ciphertext = cursor.getString(cursor.getColumnIndexOrThrow("ciphertext")),
            nonce = cursor.getString(cursor.getColumnIndexOrThrow("nonce")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updatedAt"))
        )
    }

    private fun cursorToSettingsEntity(cursor: Cursor): SettingsEntity {
        return SettingsEntity(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            key = cursor.getString(cursor.getColumnIndexOrThrow("key")),
            value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
        )
    }

    companion object {
        private const val DATABASE_NAME = "passwords.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_PASSWORDS = "passwords"
        private const val TABLE_SETTINGS = "settings"

        private const val CREATE_PASSWORDS_TABLE = """
            CREATE TABLE $TABLE_PASSWORDS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                username TEXT,
                password_encrypted TEXT,
                website TEXT,
                notes_encrypted TEXT,
                category TEXT NOT NULL,
                icon TEXT,
                ciphertext TEXT,
                nonce TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """

        private const val CREATE_SETTINGS_TABLE = """
            CREATE TABLE $TABLE_SETTINGS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                key TEXT NOT NULL UNIQUE,
                value TEXT
            )
        """
    }
}
