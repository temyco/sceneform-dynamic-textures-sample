package co.temy.sceneform

import android.net.Uri

fun String?.getNonNullString() : String{
    return this ?: ""
}

fun createUri(fileList: List<String>, folder: String, namePart: String): Uri{
    return Uri.parse("$folder/${fileList.find { it.contains(namePart) }.getNonNullString()}")
}