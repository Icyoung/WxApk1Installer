package com.icy.wxapk1installer

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.icy.wxapk1installer.Utils.getFilePathFromURI
import java.io.*
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity(){
    companion object {
        val EXTERNAL_STORAGE_PERMISSION = 1001
        val GET_UNKNOWN_APP_SOURCES = 1002
        val INSTALL_APP = 1003
    }

    lateinit var file: File
    lateinit var resUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null && intent.data != null) {
            if(creatApkCopyFile(intent.data!!.also { resUri = it }) != null){
                installApk(file)
            }
        } else {
            finish()
        }
    }

    private fun creatApkCopyFile(uri: Uri): File? {
        if (uri.scheme == ContentResolver.SCHEME_FILE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION)
            return null
        }
        val filePath = getFilePathFromURI(this, uri)
        if (filePath != null) {
            file = File(filePath)
            return file
        }
        return null
    }

    private fun installApk(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            val packageURI = Uri.parse("package:" + getPackageName())
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
            startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                FileProvider.getUriForFile(this, "com.icy.wxapk1installer.FileProvider", file)
            } else {
                Uri.fromFile(file)
            }
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            startActivityForResult(intent, INSTALL_APP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_UNKNOWN_APP_SOURCES) {
            installApk(file)
        } else if (requestCode == INSTALL_APP) {
            file.delete()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
            if (permissions[0] == READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (creatApkCopyFile(resUri) != null) {
                    installApk(file)
                }
            } else {
                finish()
            }
        }
    }

}
