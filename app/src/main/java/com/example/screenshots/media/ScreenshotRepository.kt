package com.example.screenshots.media

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.example.screenshots.model.ScreenshotItem

class ScreenshotRepository(private val context: Context) {
    fun queryScreenshots(): List<ScreenshotItem> {
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = mutableListOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection += MediaStore.Images.Media.RELATIVE_PATH
        }

        val selectionParts = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        selectionParts += "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ?"
        selectionArgs += "%Screenshots%"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selectionParts += "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            selectionArgs += "%Screenshots%"
        }

        val selection = selectionParts.joinToString(" OR ")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        return resolver.query(
            collection,
            projection.toTypedArray(),
            selection,
            selectionArgs.toTypedArray(),
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val items = mutableListOf<ScreenshotItem>()

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val takenAt = cursor.getLong(dateColumn)
                val displayName = cursor.getString(nameColumn) ?: "Screenshot"
                val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()
                    .toString()

                items += ScreenshotItem(
                    uri = uri,
                    takenAtMillis = takenAt,
                    displayName = displayName
                )
            }
            items
        } ?: emptyList()
    }
}
