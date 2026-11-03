package com.smekhnyov.carts.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class CardsDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CARDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_CODE TEXT NOT NULL,
                $COLUMN_FORMAT TEXT NOT NULL,
                $COLUMN_PALETTE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_EMBLEM_EMOJI TEXT,
                $COLUMN_EMBLEM_IMAGE TEXT,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX index_cards_created_at ON $TABLE_CARDS ($COLUMN_CREATED_AT DESC)",
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Первая версия схемы. Будущие миграции добавляются здесь.
    }

    fun readAll(): List<LoyaltyCard> {
        val result = mutableListOf<LoyaltyCard>()
        readableDatabase.query(
            TABLE_CARDS,
            ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC, $COLUMN_ID DESC",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += cursor.toLoyaltyCard()
            }
        }
        return result
    }

    fun readById(id: Long): LoyaltyCard? {
        readableDatabase.query(
            TABLE_CARDS,
            ALL_COLUMNS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toLoyaltyCard() else null
        }
    }

    fun insert(draft: CardDraft, emblemImagePath: String?, createdAt: Long): Long {
        val values = ContentValues().apply {
            put(COLUMN_NAME, draft.name.trim())
            put(COLUMN_CODE, draft.code.trim())
            put(COLUMN_FORMAT, draft.format.name)
            put(COLUMN_PALETTE, draft.paletteIndex)
            put(COLUMN_EMBLEM_EMOJI, draft.emblemEmoji)
            put(COLUMN_EMBLEM_IMAGE, emblemImagePath)
            put(COLUMN_CREATED_AT, createdAt)
        }
        return writableDatabase.insertOrThrow(TABLE_CARDS, null, values)
    }

    fun update(id: Long, draft: CardDraft, emblemImagePath: String?): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_NAME, draft.name.trim())
            put(COLUMN_CODE, draft.code.trim())
            put(COLUMN_FORMAT, draft.format.name)
            put(COLUMN_PALETTE, draft.paletteIndex)
            put(COLUMN_EMBLEM_EMOJI, draft.emblemEmoji)
            put(COLUMN_EMBLEM_IMAGE, emblemImagePath)
        }
        return writableDatabase.update(
            TABLE_CARDS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
        ) > 0
    }

    fun delete(id: Long) {
        writableDatabase.delete(TABLE_CARDS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    private fun Cursor.toLoyaltyCard(): LoyaltyCard = LoyaltyCard(
        id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
        name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
        code = getString(getColumnIndexOrThrow(COLUMN_CODE)),
        format = CardFormat.fromStorage(
            getString(getColumnIndexOrThrow(COLUMN_FORMAT)),
        ),
        paletteIndex = getInt(getColumnIndexOrThrow(COLUMN_PALETTE)),
        emblemEmoji = getString(getColumnIndexOrThrow(COLUMN_EMBLEM_EMOJI)),
        emblemImagePath = getString(getColumnIndexOrThrow(COLUMN_EMBLEM_IMAGE)),
        createdAt = getLong(getColumnIndexOrThrow(COLUMN_CREATED_AT)),
    )

    companion object {
        private const val DATABASE_NAME = "carts.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CARDS = "cards"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CODE = "code"
        const val COLUMN_FORMAT = "format"
        const val COLUMN_PALETTE = "palette"
        const val COLUMN_EMBLEM_EMOJI = "emblem_emoji"
        const val COLUMN_EMBLEM_IMAGE = "emblem_image"
        const val COLUMN_CREATED_AT = "created_at"

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_CODE,
            COLUMN_FORMAT,
            COLUMN_PALETTE,
            COLUMN_EMBLEM_EMOJI,
            COLUMN_EMBLEM_IMAGE,
            COLUMN_CREATED_AT,
        )
    }
}
