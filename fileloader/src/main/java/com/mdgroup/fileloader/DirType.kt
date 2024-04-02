package com.mdgroup.fileloader

enum class DirType {
    /**
     * Only your app can access. { android FilesDir() }
     */
    DIR_INTERNAL,

    /**
     * Only your app can access, can be deleted by system. { android CacheDir() }
     */
    DIR_CACHE,

    /**
     * Accessible by all apps but not by users. { android ExternalFilesDir() }
     */
    DIR_EXTERNAL_PRIVATE,

    /**
     * Accessible by all apps and users. { android ExternalStorageDirectory() }
     */
    DIR_EXTERNAL_PUBLIC;
}