package com.icy.wxapk1installer

import android.content.Context
import android.net.Uri
import android.text.TextUtils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

import android.content.ContentResolver.SCHEME_CONTENT
import android.content.ContentResolver.SCHEME_FILE

object Utils {

    //拷贝APK文件到App内并去除.1后缀
    fun getFilePathFromURI(context: Context, contentUri: Uri): String? {
        val cacheDataDir = context.cacheDir
        val fileName = getFileNameRemoveSuffix(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile = File(cacheDataDir.toString() + File.separator + fileName)
            if (copyApkFile(context, contentUri, copyFile)) {
                return copyFile.absolutePath
            }
        }
        return null
    }

    //获取去掉.1后的文件名
    private fun getFileNameRemoveSuffix(uri: Uri): String? {
        if (!uri.path!!.endsWith(".apk.1")) return null
        var fileName: String? = null
        val path = uri.path
        val cut = path!!.lastIndexOf('/')
        val end = path.lastIndexOf('.')
        if (cut != -1) {
            fileName = path.substring(cut + 1, end)
        }
        return fileName
    }

    //读取Uri拷贝Apk
    private fun copyApkFile(context: Context, uri: Uri, file: File): Boolean {
        try {
            //解析外部传来的是File:// 还是 Content://
            val inputStream: InputStream = if (SCHEME_FILE == uri.scheme) {
                //需要读取存储权限
                FileInputStream(File(uri.path!!))
            } else if (SCHEME_CONTENT == uri.scheme) {
                context.contentResolver.openInputStream(uri)
            } else {
                return false
            }

            val outputStream = FileOutputStream(file)

            val BUFFER_SIZE = 1024 * 2
            var n: Int
            val buffer = ByteArray(BUFFER_SIZE)
            val bis = BufferedInputStream(inputStream, BUFFER_SIZE)
            val bos = BufferedOutputStream(outputStream, BUFFER_SIZE)
            while (bis.read(buffer).also { n = it } != -1) {
                bos.write(buffer, 0, n)
            }
            bos.flush()
            bos.close()
            bis.close()
            inputStream.close()
            outputStream.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

}
