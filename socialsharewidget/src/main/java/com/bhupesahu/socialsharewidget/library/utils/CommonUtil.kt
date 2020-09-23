package com.bhupesahu.socialsharewidget.library.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException

object CommonUtil {
    @JvmStatic
    fun isValidString(s: String?): Boolean {
        return s != null && !s.trim { it <= ' ' }.isEmpty() && !s.equals("null", ignoreCase = true)
    }

    @JvmStatic
    fun showToast(context: Context?, msg: String?) {
        if (context == null || !isValidString(msg)) return
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun getShareIntent(
        context: Context, title: String, description: String?, imgUrl: String?,
        callback: OnIntentPreparedCallback?
    ) {
        try {
            val newsDetail = "*$title*\n\n$description"
            if (!isValidString(imgUrl)) {
                val sendIntent = Intent(Intent.ACTION_SEND)
                sendIntent.type = "text/plain"
                sendIntent.putExtra(Intent.EXTRA_TEXT, newsDetail)
                callback?.onIntentPrepared(sendIntent)
            } else {
                Picasso.get()
                    .load(imgUrl)
                    .into(object : Target {
                        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
                            val bmpUri = getLocalBitmapUri(bitmap, context)
                            val sendIntent = Intent(Intent.ACTION_SEND)
                            sendIntent.type = "image/*"
                            sendIntent.putExtra(Intent.EXTRA_TEXT, newsDetail)
                            sendIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
                            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            callback?.onIntentPrepared(sendIntent)
                        }

                        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable) {}
                        override fun onPrepareLoad(placeHolderDrawable: Drawable) {}
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(context, "Something went wrong!")
        }
    }

    fun getBitmapFromUri(uri: Uri?, context: Context?): Bitmap? {
        val fileDescriptor: FileDescriptor
        var image: Bitmap? = null
        if (context == null) return image
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(
                uri!!, "r"
            )
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.fileDescriptor
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return image
    }

    fun getLocalBitmapUri(bmp: Bitmap, context: Context): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image_" + System.currentTimeMillis() + ".png"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bmpUri
    }

    interface OnIntentPreparedCallback {
        fun onIntentPrepared(intent: Intent)
    }
}