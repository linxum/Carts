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
                $COLUMN_NOTE TEXT,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_POSITION INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX index_cards_created_at ON $TABLE_CARDS ($COLUMN_CREATED_AT DESC)",
        )
        db.execSQL(
            "CREATE INDEX index_cards_position ON $TABLE_CARDS ($COLUMN_POSITION ASC)",
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE $TABLE_CARDS ADD COLUMN $COLUMN_POSITION INTEGER NOT NULL DEFAULT 0",
            )

            val orderedIds = mutableListOf<Long>()
            db.query(
                TABLE_CARDS,
                arrayOf(COLUMN_ID),
                null,
                null,
                null,
                null,
                "$COLUMN_CREATED_AT DESC, $COLUMN_ID DESC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    orderedIds += cursor.getLong(0)
                }
            }

            db.beginTransaction()
            try {
                orderedIds.forEachIndexed { index, id ->
                    updatePosition(db, id, index)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_cards_position " +
                    "ON $TABLE_CARDS ($COLUMN_POSITION ASC)",
            )
        }
        if (oldVersion < 3) {
            db.execSQL(
                "ALTER TABLE $TABLE_CARDS ADD COLUMN $COLUMN_NOTE TEXT",
            )
        }
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
            "$COLUMN_POSITION ASC, $COLUMN_ID ASC",
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
        val db = writableDatabase
        db.beginTransaction()
        try {
            val position = db.rawQuery(
                "SELECT COALESCE(MAX($COLUMN_POSITION), -1) + 1 FROM $TABLE_CARDS",
                null,
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }

            val values = ContentValues().apply {
                put(COLUMN_NAME, draft.name.trim())
                put(COLUMN_CODE, draft.code.trim())
                put(COLUMN_FORMAT, draft.format.name)
                put(COLUMN_PALETTE, draft.paletteIndex)
                put(COLUMN_EMBLEM_EMOJI, draft.emblemEmoji)
                put(COLUMN_EMBLEM_IMAGE, emblemImagePath)
                put(COLUMN_NOTE, draft.note.trim().ifEmpty { null })
                put(COLUMN_CREATED_AT, createdAt)
                put(COLUMN_POSITION, position)
            }
            val id = db.insertOrThrow(TABLE_CARDS, null, values)
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun update(id: Long, draft: CardDraft, emblemImagePath: String?): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_NAME, draft.name.trim())
            put(COLUMN_CODE, draft.code.trim())
            put(COLUMN_FORMAT, draft.format.name)
            put(COLUMN_PALETTE, draft.paletteIndex)
            put(COLUMN_EMBLEM_EMOJI, draft.emblemEmoji)
            put(COLUMN_EMBLEM_IMAGE, emblemImagePath)
            put(COLUMN_NOTE, draft.note.trim().ifEmpty { null })
        }
        return writableDatabase.update(
            TABLE_CARDS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
        ) > 0
    }

    fun move(cardId: Long, direction: Int): Boolean {
        if (direction == 0) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val entries = mutableListOf<Pair<Long, Int>>()
            db.query(
                TABLE_CARDS,
                arrayOf(COLUMN_ID, COLUMN_POSITION),
                null,
                null,
                null,
                null,
                "$COLUMN_POSITION ASC, $COLUMN_ID ASC",
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    entries += cursor.getLong(0) to cursor.getInt(1)
                }
            }

            val index = entries.indexOfFirst { it.first == cardId }
            val target = index + direction
            if (index < 0 || target !in entries.indices) return false

            val (firstId, firstPosition) = entries[index]
            val (secondId, secondPosition) = entries[target]
            updatePosition(db, firstId, secondPosition)
            updatePosition(db, secondId, firstPosition)
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun delete(id: Long) {
        writableDatabase.delete(TABLE_CARDS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAll() {
        writableDatabase.delete(TABLE_CARDS, null, null)
    }

    private fun updatePosition(db: SQLiteDatabase, id: Long, position: Int) {
        val values = ContentValues().apply {
            put(COLUMN_POSITION, position)
        }
        db.update(TABLE_CARDS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
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
        position = getInt(getColumnIndexOrThrow(COLUMN_POSITION)),
        note = getString(getColumnIndexOrThrow(COLUMN_NOTE)),
    )

    companion object {
        private const val DATABASE_NAME = "carts.db"
        private const val DATABASE_VERSION = 3

        const val TABLE_CARDS = "cards"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CODE = "code"
        const val COLUMN_FORMAT = "format"
        const val COLUMN_PALETTE = "palette"
        const val COLUMN_EMBLEM_EMOJI = "emblem_emoji"
        const val COLUMN_EMBLEM_IMAGE = "emblem_image"
        const val COLUMN_NOTE = "note"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_POSITION = "position"

        private val ALL_COLUMNS = arrayOf(
            COLUMN_ID,
            COLUMN_NAME,
            COLUMN_CODE,
            COLUMN_FORMAT,
            COLUMN_PALETTE,
            COLUMN_EMBLEM_EMOJI,
            COLUMN_EMBLEM_IMAGE,
            COLUMN_NOTE,
            COLUMN_CREATED_AT,
            COLUMN_POSITION,
        )
    }
}
