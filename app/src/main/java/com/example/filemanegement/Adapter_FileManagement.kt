package com.example.filemanegement

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.io.File

class Adapter_FileManagement(private var mutableList: MutableList<FileItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var shareViewModel: ShareViewModel
    lateinit var context: Context

    companion object {
        private const val VIEW_TYPE_FOLDER = 1
        private const val VIEW_TYPE_FILE = 2
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<FileItem>) {
        mutableList.clear()
        mutableList.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        shareViewModel = (parent.context as MainActivity).getShareViewModel()
        context = parent.context
        return if (viewType == VIEW_TYPE_FOLDER) {
            val view = inflater.inflate(R.layout.item_folder, parent, false)
            Item_Folder(view)
        } else {
            val view = inflater.inflate(R.layout.item_file, parent, false)
            Item_File(view)
        }
    }

    override fun getItemCount(): Int {
        return mutableList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mutableList[position]
        if (holder.itemViewType == VIEW_TYPE_FOLDER && holder is Item_Folder) {
            holder.bind(item)
            holder.itemView.setOnClickListener {
                mutableList = item.listFile!!
                shareViewModel.currentPath.value = item.path
                notifyDataSetChanged()
            }
            holder.itemView.setOnLongClickListener {
                showPopupMenu(holder, position)
                true
            }
        } else if (holder.itemViewType == VIEW_TYPE_FILE && holder is Item_File) {
            holder.bind(item)
            holder.itemView.setOnClickListener {
                val file = File(item.path)

                // Kiểm tra loại file và xử lý tương ứng
                if (isTextFile(file)) {
                    // Nếu là file văn bản (TXT), mở Intent để xem văn bản
                    openTextFile(file)
                } else if (isImageFile(file)) {
                    // Nếu là file ảnh (BMP, JPG, PNG), mở Intent để xem ảnh
                    openImageFile(file)
                }
            }
            holder.itemView.setOnLongClickListener {
                showPopupMenu(holder, position)
                true
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mutableList[position].isFolder) {
            VIEW_TYPE_FOLDER
        } else {
            VIEW_TYPE_FILE
        }
    }

    class Item_Folder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val nameFolder = itemview.findViewById<TextView>(R.id.nameFolder)
        fun bind(item: FileItem) {
            nameFolder.text = item.name
        }
    }

    class Item_File(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val imgView = itemview.findViewById<ImageView>(R.id.file_img)
        val namefile = itemview.findViewById<TextView>(R.id.nameFile)
        fun bind(item: FileItem) {
            namefile.text = item.name
        }
    }

    private fun showPopupMenu(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        val popupMenu = PopupMenu(context, view)
        if (holder.itemViewType == VIEW_TYPE_FOLDER) {
            popupMenu.menuInflater.inflate(R.menu.context_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.rename -> {
                        showRenameDialog(context, position)
                        true
                    }

                    R.id.remove -> {
                        deleteFile(position)
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        } else if (holder.itemViewType == VIEW_TYPE_FILE) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.context_menu_file, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.renamefile -> {
                        showRenameDialog(context, position)
                        true
                    }

                    R.id.removefile -> {
                        deleteFile(position)
                        true
                    }

                    R.id.move -> {
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun isTextFile(file: File): Boolean {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        return mimeType != null && mimeType.startsWith("text")
    }

    private fun isImageFile(file: File): Boolean {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        val mimeType =
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
        return mimeType != null && mimeType.startsWith("image")
    }

    private fun openTextFile(file: File) {
        val uri =
            FileProvider.getUriForFile(context, "com.example.filemanegement.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(context, intent, null)
        } catch (e: ActivityNotFoundException) {
            // Xử lý khi không có ứng dụng xem văn bản nào được cài đặt
            Toast.makeText(context, "No text viewer installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImageFile(file: File) {
        val uri =
            FileProvider.getUriForFile(context, "com.example.filemanegement.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(context, intent, null)
        } catch (e: ActivityNotFoundException) {
            // Xử lý khi không có ứng dụng xem ảnh nào được cài đặt
            Toast.makeText(context, "No image viewer installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(position: Int): Boolean {
        val file = File(mutableList[position].path)
        if (file.delete()) {
            mutableList.removeAt(position)
            notifyDataSetChanged()
            return true
        } else return false
    }

    fun renameFileOrFolder(position: Int, newName: String): Boolean {
        val oldFile = File(mutableList[position].path)
        if (!oldFile.exists()) {
            return false
        }
        val parentPath = oldFile.parent
        val newFile = File(parentPath, newName)
        oldFile.renameTo(newFile)
        mutableList[position].name = newName
        mutableList[position].path = newFile.path
        notifyDataSetChanged()
            return true
    }

    private fun showRenameDialog(context: Context, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Rename File")
        val oldName = mutableList[position].name
        val input = EditText(context)
        input.setText(oldName)

        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val newName = input.text.toString()
            // Xử lý logic đổi tên ở đây
            renameFileOrFolder(position, newName)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}