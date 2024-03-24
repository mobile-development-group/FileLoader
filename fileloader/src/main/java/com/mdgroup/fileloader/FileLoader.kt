package com.mdgroup.fileloader

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FileLoader(private val context: Context) {

    companion object {
        const val KEY_PROGRESS = LoaderWorker.KEY_PROGRESS
        const val OUTPUT_URIS = LoaderWorker.OUTPUT_URIS
        const val OUTPUT_ERROR = LoaderWorker.OUTPUT_ERROR

        /**
         * Only your app can access. { android FilesDir() }
         */
        const val DIR_INTERNAL = AndroidFileManager.DIR_INTERNAL

        /**
         * Only your app can access, can be deleted by system. { android CacheDir() }
         */
        const val DIR_CACHE = AndroidFileManager.DIR_CACHE

        /**
         * Accessible by all apps but not by users. { android ExternalFilesDir() }
         */
        const val DIR_EXTERNAL_PRIVATE = AndroidFileManager.DIR_EXTERNAL_PRIVATE

        /**
         * Accessible by all apps and users. { android ExternalStorageDirectory() }
         */
        const val DIR_EXTERNAL_PUBLIC = AndroidFileManager.DIR_EXTERNAL_PUBLIC
    }

    private val DEFAULT_TAG = "FileDownloader"

    private var downloadWorkManager: WorkManager = WorkManager.getInstance(context)

    fun load(
        url: String,
        tag: String?,
        directoryName: String? = null,
        directoryType: Int? = null,
        headers: Map<String, String>? = null,
        isCookie: Boolean = false
    ) = load(
        listOf(url),
        tag,
        directoryName,
        directoryType,
        headers,
        isCookie
    )

    fun load(
        urls: List<String>,
        tag: String? = DEFAULT_TAG,
        directoryName: String? = null,
        directoryType: Int? = null,
        headers: Map<String, String>? = null,
        isCookie: Boolean = false
    ): UUID = WorkUtils.makeDownloadRequest(
        workManager = downloadWorkManager,
        urls = urls,
        tag = tag,
        directoryName = directoryName,
        directoryType = directoryType,
        headers = headers,
        isCookie = isCookie
    )

    fun getWorkInfosFlow(tag: String = DEFAULT_TAG): Flow<List<WorkInfo>> =
        downloadWorkManager.getWorkInfosByTagFlow(tag)

    fun getWorkInfoByIdFlow(uuid: UUID): Flow<WorkInfo> =
        downloadWorkManager.getWorkInfoByIdFlow(uuid)

    fun getWorkInfoByIdLiveData(uuid: UUID): LiveData<WorkInfo> =
        downloadWorkManager.getWorkInfoByIdLiveData(uuid)

    fun getWorkInfosByTagLiveData(tag: String = DEFAULT_TAG): LiveData<List<WorkInfo>> =
        downloadWorkManager.getWorkInfosByTagLiveData(tag)
}