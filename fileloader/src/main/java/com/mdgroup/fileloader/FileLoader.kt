package com.mdgroup.fileloader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

class FileLoader(private val context: Context) {

    companion object {
        const val KEY_PROGRESS = LoaderWorker.KEY_PROGRESS
        const val OUTPUT_URIS = LoaderWorker.OUTPUT_URIS
        const val OUTPUT_ERROR = LoaderWorker.OUTPUT_ERROR

        private const val TAG = "FileLoader"
    }

    private var downloadWorkManager: WorkManager = WorkManager.getInstance(context)

    fun load(
        url: String,
        tag: String? = TAG,
        fileNamePrefix: String? = null,
        fileExtension: FileExtension = FileExtension.UNKNOWN,
        directoryName: String? = null,
        directoryType: DirType? = null,
        headers: Map<String, String>? = null,
        isCookie: Boolean = false
    ) = load(
        listOf(url),
        tag,
        fileNamePrefix,
        fileExtension,
        directoryName,
        directoryType,
        headers,
        isCookie
    )

    fun load(
        urls: List<String>,
        tag: String? = TAG,
        fileNamePrefix: String? = null,
        fileExtension: FileExtension = FileExtension.UNKNOWN,
        directoryName: String? = null,
        directoryType: DirType? = null,
        headers: Map<String, String>? = null,
        isCookie: Boolean = false
    ): UUID = WorkUtils.makeDownloadRequest(
        workManager = downloadWorkManager,
        urls = urls,
        tag = tag,
        fileNamePrefix = fileNamePrefix,
        fileExtension = fileExtension,
        directoryName = directoryName,
        directoryType = directoryType,
        headers = headers,
        isCookie = isCookie
    )

    fun getWorkInfosFlow(tag: String = TAG): Flow<List<WorkInfo>> =
        downloadWorkManager.getWorkInfosByTagFlow(tag)

    fun getWorkInfoByIdFlow(uuid: UUID): Flow<WorkInfo> =
        downloadWorkManager.getWorkInfoByIdFlow(uuid)

    fun getWorkInfoByIdLiveData(uuid: UUID): LiveData<WorkInfo> =
        downloadWorkManager.getWorkInfoByIdLiveData(uuid)

    fun getWorkInfosByTagLiveData(tag: String = TAG): LiveData<List<WorkInfo>> =
        downloadWorkManager.getWorkInfosByTagLiveData(tag)

    fun remove(uri: String) = remove(listOf(uri))

    fun remove(uris: List<String>): Int {
        var count = 0
        uris.forEach { path ->
            val file = AndroidFileManager.getFileFromUri(context, Uri.parse(path))

            if (file.exists()) {
                file.delete()
                count++
            }
        }
        return count
    }

    fun clearDirectory(directoryName: String, directoryType: DirType): Int {
        val directory =
            AndroidFileManager.getAppropriateDirectory(context, directoryName, directoryType)
        var count = 0
        directory.listFiles()?.forEach { file ->
            file.delete()
            count++
        }
        return count
    }

    fun getFileByUri(uri: String) : File =
        AndroidFileManager.getFileFromUri(context, Uri.parse(uri))
}