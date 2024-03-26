package com.mdgroup.fileloader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.util.regex.Pattern

internal object AndroidFileManager {

    /**
     * Only your app can access. { android FilesDir() }
     */
    const val DIR_INTERNAL = 1

    /**
     * Only your app can access, can be deleted by system. { android CacheDir() }
     */
    const val DIR_CACHE = 2

    /**
     * Accessible by all apps but not by users. { android ExternalFilesDir() }
     */
    const val DIR_EXTERNAL_PRIVATE = 3

    /**
     * Accessible by all apps and users. { android ExternalStorageDirectory() }
     */
    const val DIR_EXTERNAL_PUBLIC = 4

    val isExternalStorageReadable: Boolean
        /* Checks if external storage is available to at least read */
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    @Throws(Exception::class)
    fun getFileForRequest(
        context: Context,
        fileUri: String,
        fileNamePrefix: String,
        fileExtension: String,
        dirName: String,
        dirType: Int
    ): File = File(
        getAppropriateDirectory(context, dirName, dirType),
        getFileName(fileUri, fileNamePrefix, fileExtension)
    )

    @Throws(Exception::class)
    fun getFileName(uri: String, fileNamePrefix: String, fileExtension: String = ""): String {
        var fileName: String?
        try {
            // Passed uri is valid url, create file name as hashcode of url
            val url = URL(uri)
            fileName = getFileNameFromUrl(url)
            if (fileName == null) {
                fileName = uri.hashCode().toString()
            }
        } catch (e: MalformedURLException) {
            if (uri.contains("/")) {
                throw Exception("File name should not contain path separator \"/\"")
            }
            // Passed uri is name of the file
            fileName = uri
        }
        // Replace spaces to underlines
        fileName = fileName?.replace("%20", "_")
        // Replace all other screened characters
        fileName = URLDecoder.decode(fileName, "UTF-8")
        return fileNamePrefix + fileName + fileExtension
    }

    @Throws(Exception::class)
    fun getAppropriateDirectory(
        context: Context,
        directoryName: String,
        directoryType: Int
    ): File {
        val file = when (directoryType) {
            DIR_CACHE -> File(context.cacheDir, directoryName)
            DIR_EXTERNAL_PRIVATE -> getExternalPrivateDirectory(
                context,
                directoryName
            )

            DIR_EXTERNAL_PUBLIC -> getExternalPublicDirectory(directoryName)
            else -> File(context.filesDir, directoryName)
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    @Throws(IOException::class)
    fun getFileFromUri(context: Context, uri: Uri): File {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val file = File(uri.toString())
        return if (file.exists()) {
            file
        } else {
            val imageStream = context.contentResolver.openInputStream(uri)
            imageStream.use { stream ->
                file.createNewFile()
                file.outputStream().use { fileOut ->
                    stream?.copyTo(fileOut)
                }
                file
            }
        }
    }

    fun readFileAsString(file: File): String {
        val sb = StringBuilder()
        try {
            if (file.exists()) {
                val isr = InputStreamReader(FileInputStream(file))
                val bufferedReader = BufferedReader(isr)
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return sb.toString()
    }

    fun getBitmap(downloadedFile: File): Bitmap {
        return BitmapFactory.decodeFile(downloadedFile.path)
    }

    @Throws(Exception::class)
    fun searchAndGetLocalFile(
        context: Context,
        uri: String,
        fileNamePrefix: String,
        dirName: String,
        dirType: Int
    ): File? {
        var foundFile: File? = null
        if (!TextUtils.isEmpty(dirName)) {
            val dir = getAppropriateDirectory(context, dirName, dirType)
            if (dir.exists()) {
                val allFiles = dir.listFiles()
                if (allFiles != null) {
                    for (file in allFiles) {
                        if (!file.isDirectory && file.name == getFileName(
                                uri,
                                fileNamePrefix
                            )
                        ) {
                            foundFile = file
                            break
                        }
                    }
                }
            }
        }
        return foundFile
    }

    private fun isValidFileName(fileName: String): Boolean {
        val pattern = Pattern.compile(".*\\..*")
        return pattern.matcher(fileName).matches()
    }

    private fun isExternalStorageWritable(): Boolean {
        /* Checks if external storage is available for read and write */
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun getFileNameFromUrl(url: URL): String? {
        var fileName: String? = null
        val path = url.path
        if (path != null) {
            val pathArr = path
                .split("/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (pathArr.isNotEmpty()) {
                val lastPath = pathArr[pathArr.size - 1]
                if (isValidFileName(lastPath)) {
                    fileName = lastPath
                }
            }
        }
        return fileName
    }

    @Throws(Exception::class)
    private fun getExternalPublicDirectory(directoryName: String): File {
        return if (isExternalStorageWritable()) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
                it
            } ?: run {
                File(Environment.getExternalStorageDirectory().absolutePath, directoryName)
            }
        } else {
            throw Exception("External storage is not available for write operation")
        }
    }

    @Throws(Exception::class)
    private fun getExternalPrivateDirectory(context: Context, directoryName: String): File {
        val baseDir = if (isExternalStorageWritable()) {
            val baseDirFile = context.getExternalFilesDir(null)
            if (baseDirFile == null) {
                context.filesDir.absolutePath
            } else {
                baseDirFile.absolutePath
            }
        } else {
            throw Exception("External storage is not available for write operation")
        }
        return File(baseDir, directoryName)
    }
}