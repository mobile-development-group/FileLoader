package com.mdgroup.fileloader

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal class Loader private constructor(
    context: Context,
    private val url: String,
    private val fileNamePrefix: String = "",
    private val fileExtension: String = "",
    private val dirName: String,
    private val dirType: DirType,
    private val headers: Map<String, String>?,
    private val isCookie: Boolean
) {

    companion object {
        private const val TAG = "FileLoader"

        private const val MEGABYTE = 1024 * 1024
    }

    private val context: Context

    init {
        this.context = context.applicationContext
    }

    @WorkerThread
    @Throws(Exception::class)
    fun download(): File {
        val fileName = AndroidFileManager.getFileName(url, fileNamePrefix)
        var downloadFilePath: File = AndroidFileManager.getFileForRequest(
            context,
            url,
            fileNamePrefix,
            fileExtension,
            dirName,
            dirType
        )

        val urlConnection: HttpsURLConnection = URL(url).openConnection() as HttpsURLConnection

        headers?.entries?.forEach {
            urlConnection.addRequestProperty(it.key, it.value)
        }

        if (isCookie) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)
        }

        val values = ContentValues()

        values.put(MediaStore.MediaColumns.TITLE, fileName)
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        values.put(
            MediaStore.MediaColumns.DATE_ADDED,
            System.currentTimeMillis() / 1000
        )

        // If file already exists, delete it
        if (downloadFilePath.exists() && downloadFilePath.delete()) {
            downloadFilePath = AndroidFileManager.getFileForRequest(
                context,
                url,
                fileNamePrefix,
                fileExtension,
                dirName,
                dirType
            )
        }

        // Write the body to file
        Log.d(TAG, "Start download: $url")
        urlConnection.connect()

        if (urlConnection.responseCode != 200) {
            throw IOException("Failed to download file. Response code: ${urlConnection.responseCode}")
        }

        val inputStream: InputStream = urlConnection.inputStream
        val outputStream = FileOutputStream(downloadFilePath)
        val buffer = ByteArray(MEGABYTE)
        var actualSize = 0
        var bufferLength: Int
        while (inputStream.read(buffer).also { bufferLength = it } > 0) {
            actualSize += bufferLength
            outputStream.write(buffer, 0, bufferLength)
        }
        outputStream.close()

        // Check if downloaded file is not corrupt
        var contentLength = 0
        try {
            contentLength = urlConnection.getHeaderField("Content-Length").toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        if (actualSize < contentLength) {
            downloadFilePath.delete()
            throw IOException("Failed to download file. Content-Length: $contentLength")
        }

        urlConnection.disconnect()

        Log.d(TAG, "Success download: $downloadFilePath")

        return downloadFilePath
    }

    class Builder {
        private var url: String? = null
        private var dirName: String? = null
        private var dirType = DirType.DIR_EXTERNAL_PUBLIC
        private var context: Context? = null
        private var fileNamePrefix = ""
        private var fileExtension = ""
        private var headers: Map<String, String>? = null
        private var isCookie: Boolean = false

        fun setUrl(uri: String?): Builder {
            this.url = uri
            return this
        }

        fun setDirName(dirName: String?): Builder {
            this.dirName = dirName
            return this
        }

        fun setDirType(dirType: DirType): Builder {
            this.dirType = dirType
            return this
        }

        fun setContext(context: Context?): Builder {
            this.context = context
            return this
        }

        fun setFileNamePrefix(fileNamePrefix: String): Builder {
            this.fileNamePrefix = fileNamePrefix
            return this
        }

        fun setFileExtension(fileExtension: String): Builder {
            this.fileExtension = fileExtension
            return this
        }

        fun setHeaders(headers: Map<String, String>?): Builder {
            this.headers = headers
            return this
        }

        fun setCookie(isCookie: Boolean): Builder {
            this.isCookie = isCookie
            return this
        }

        fun build(): Loader {
            return Loader(
                context ?: throw Exception("context cant by NULL"),
                url ?: throw Exception("Url cant by NULL"),
                fileNamePrefix,
                fileExtension,
                dirName ?: throw Exception("dirName cant by NULL"),
                dirType,
                headers,
                isCookie
            )
        }
    }
}