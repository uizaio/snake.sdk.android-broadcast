package com.uiza.util

import android.content.Context
import java.io.File

object UZPathUtils {

    @JvmStatic
    fun getRecordPath(context: Context): File {
        return File(
            context.getExternalFilesDir(null)?.absolutePath + "/${context.packageName}"
        )
    }

//    @JvmStatic
//    fun getPath(context: Context, uri: Uri): String? {
//        // DocumentProvider
//        when {
//            DocumentsContract.isDocumentUri(context, uri) -> {
//                when {
//                    isExternalStorageDocument(uri) -> { // ExternalStorageProvider
//                        val docId = DocumentsContract.getDocumentId(uri)
//                        val split = docId.split(":".toRegex()).toTypedArray()
//                        val type = split[0]
//                        val storageDefinition: String
//                        return if ("primary".equals(type, ignoreCase = true)) {
//                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
//                        } else {
//                            storageDefinition = if (Environment.isExternalStorageRemovable()) {
//                                "EXTERNAL_STORAGE"
//                            } else {
//                                "SECONDARY_STORAGE"
//                            }
//                            System.getenv(storageDefinition) + "/" + split[1]
//                        }
//                    }
//                    isDownloadsDocument(uri) -> { // DownloadsProvider
//                        val id = DocumentsContract.getDocumentId(uri)
//                        val contentUri = ContentUris.withAppendedId(
//                            Uri.parse("content://downloads/public_downloads"),
//                            id.toLong()
//                        )
//                        return getDataColumn(context, contentUri, null, null)
//                    }
//                    isMediaDocument(uri) -> { // MediaProvider
//                        val docId = DocumentsContract.getDocumentId(uri)
//                        val split = docId.split(":".toRegex()).toTypedArray()
//                        val type = split[0]
//                        var contentUri: Uri? = null
//                        when (type) {
//                            "image" -> {
//                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                            }
//                            "video" -> {
//                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                            }
//                            "audio" -> {
//                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                            }
//                        }
//                        val selection = "_id=?"
//                        val selectionArgs = arrayOf(
//                            split[1]
//                        )
//                        return getDataColumn(context, contentUri, selection, selectionArgs)
//                    }
//                }
//            }
//            "content".equals(uri.scheme, ignoreCase = true) -> { // MediaStore (and general)
//
//                // Return the remote address
//                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
//                    context,
//                    uri,
//                    null,
//                    null
//                )
//            }
//            "file".equals(uri.scheme, ignoreCase = true) -> { // File
//                return uri.path
//            }
//        }
//        return null
//    }

//    private fun getDataColumn(
//        context: Context,
//        uri: Uri?,
//        selection: String?,
//        selectionArgs: Array<String>?
//    ): String? {
//        var cursor: Cursor? = null
//        val column = "_data"
//        val projection = arrayOf(column)
//        try {
//            cursor = uri?.let {
//                context.contentResolver.query(
//                    it,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    null
//                )
//            }
//            if (cursor != null && cursor.moveToFirst()) {
//                val columnIndex = cursor.getColumnIndexOrThrow(column)
//                return cursor.getString(columnIndex)
//            }
//        } finally {
//            cursor?.close()
//        }
//        return null
//    }

//    private fun isExternalStorageDocument(uri: Uri): Boolean {
//        return "com.android.externalstorage.documents" == uri.authority
//    }

//    private fun isDownloadsDocument(uri: Uri): Boolean {
//        return "com.android.providers.downloads.documents" == uri.authority
//    }

//    private fun isMediaDocument(uri: Uri): Boolean {
//        return "com.android.providers.media.documents" == uri.authority
//    }

//    private fun isGooglePhotosUri(uri: Uri): Boolean {
//        return "com.google.android.apps.photos.content" == uri.authority
//    }
}
