package com.mdgroup.fileloader

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.UUID

internal object WorkUtils {

    fun makeDownloadRequest(
        workManager: WorkManager,
        urls: List<String>,
        tag: String?,
        fileNamePrefix: String? = null,
        fileExtension: FileExtension = FileExtension.UNKNOWN,
        directoryName: String? = null,
        directoryType: DirType? = null,
        headers: Map<String, String>? = null,
        isCookie: Boolean = false
    ): UUID {
        workManager.pruneWork()

        val inputData = Data.Builder().apply {
            if (urls.isNotEmpty()) {
                putStringArray(LoaderWorker.KEY_DOWNLOAD_URLS, urls.toTypedArray())
            }

            putString(LoaderWorker.KEY_FILE_NAME_PREFIX, fileNamePrefix)
            putString(LoaderWorker.KEY_FILE_EXTENSION, fileExtension.value)

            putString(LoaderWorker.KEY_DIRECTORY_NAME, directoryName)
            if (directoryType != null) {
                putString(LoaderWorker.KEY_DIRECTORY_TYPE, directoryType.name)
            }

            if (!headers.isNullOrEmpty()) {
                putStringArray(LoaderWorker.KEY_HEADERS_NAMES, headers.keys.toTypedArray())
                putStringArray(LoaderWorker.KEY_HEADERS_VALUES, headers.values.toTypedArray())
            }
            putBoolean(LoaderWorker.KEY_IS_COOKIE, isCookie)
        }.build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequest.Builder(LoaderWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .apply {
                tag?.let { addTag(it) }
            }
            .build()

        workManager.enqueue(workRequest)

        return workRequest.id
    }
}