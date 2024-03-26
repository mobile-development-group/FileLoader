package com.mdgroup.fileloader

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException

internal class LoaderWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    companion object {

        private const val TAG = "LoaderWorker"

        const val KEY_DOWNLOAD_URLS = "KEY_DOWNLOAD_URLS"
        const val KEY_FILE_NAME_PREFIX = "KEY_FILE_NAME_PREFIX"
        const val KEY_FILE_EXTENSION = "KEY_FILE_EXTENSION"
        const val KEY_DIRECTORY_NAME = "KEY_DIRECTORY_NAME"
        const val KEY_DIRECTORY_TYPE = "KEY_DIRECTORY_TYPE"
        const val KEY_HEADERS_NAMES = "KEY_HEADERS_NAMES"
        const val KEY_HEADERS_VALUES = "KEY_HEADERS_VALUES"
        const val KEY_IS_COOKIE = "KEY_IS_COOKIE"

        const val KEY_PROGRESS = "KEY_ACTUAL_PROGRESS"
        const val OUTPUT_URIS = "OUTPUT_URIS"
        const val OUTPUT_ERROR = "OUTPUT_ERROR"
    }

    override fun doWork(): Result {
        if (isStopped) {
            return Result.success()
        }

        val urls = inputData.getStringArray(KEY_DOWNLOAD_URLS)

        val fileNamePrefix = inputData.getString(KEY_FILE_NAME_PREFIX) ?: ""
        val fileExtension = FileExtension.parse(inputData.getString(KEY_FILE_EXTENSION))
        val directoryName =
            inputData.getString(KEY_DIRECTORY_NAME) ?: Environment.DIRECTORY_DOWNLOADS
        val directoryType =
            inputData.getInt(KEY_DIRECTORY_TYPE, AndroidFileManager.DIR_EXTERNAL_PUBLIC)

        val headersNames = inputData.getStringArray(KEY_HEADERS_NAMES)
        val headersValues = inputData.getStringArray(KEY_HEADERS_VALUES)

        val headers = if (headersNames != null && headersValues != null) {
            headersNames.zip(headersValues).toMap()
        } else {
            mapOf()
        }

        val isCookie = inputData.getBoolean(KEY_IS_COOKIE, false)

        val uris = mutableListOf<String>()

        urls?.forEachIndexed { index, url ->
            try {
                val downloader: Loader = Loader.Builder()
                    .setContext(applicationContext)
                    .setUrl(url)
                    .setDirName(directoryName)
                    .setDirType(directoryType)
                    .setFileNamePrefix(fileNamePrefix)
                    .setFileExtension(fileExtension.value)
                    .setHeaders(headers)
                    .setCookie(isCookie)
                    .build()

                val uri = downloader.download().path
                uris.add(uri)

                setProgressAsync(workDataOf(KEY_PROGRESS to index + 1))

                if (uris.size == urls.size) {
                    return Result.success(workDataOf(OUTPUT_URIS to uris.toTypedArray()))
                }
            } catch (e: FileNotFoundException) {
                Log.d(TAG, "${LoaderWorker::class.java.name}: ${e.localizedMessage}")
            } catch (e: MalformedURLException) {
                Log.d(TAG, "${LoaderWorker::class.java.name}: ${e.localizedMessage}")
            } catch (e: IOException) {
                Log.d(TAG, "${LoaderWorker::class.java.name}: ${e.localizedMessage}")
            } catch (e: Exception) {
                Log.d(TAG, "${LoaderWorker::class.java.name}: ${e.localizedMessage}")
            }
        }

        return Result.success()
    }
}