package com.example.filemanegement

data class FileItem(
    var name : String,
    var path : String,
    val isFolder: Boolean,
    val listFile: MutableList<FileItem>? = null
)