package com.mdgroup.fileloader


enum class FileExtension(val value: String) {
    UNKNOWN(""),
    JPG(".jpg"),
    JPEG(".jpeg"),
    GIF(".gif"),
    WEBM(".webm"),
    MP3(".mp3"),
    MP4(".mp4"),
    JSON(".json"),
    PDF(".pdf"),
    TEXT(".txt");

    companion object {
        fun parse(value: String?): FileExtension =
            entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}