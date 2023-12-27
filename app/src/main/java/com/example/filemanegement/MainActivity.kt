package com.example.filemanegement

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var shareViewModel: ShareViewModel
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shareViewModel = ViewModelProvider(this).get(ShareViewModel::class.java)
        button = findViewById<Button>(R.id.buttonFile)
        button.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                // Cho phiên bản Android dưới Android 11
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
                } else {
                    getRoot()
                    navigateToFragment()
                }
            } else {
                // Cho Android 11 trở lên
                if (!Environment.isExternalStorageManager()) {
                    // Yêu cầu quyền MANAGE_EXTERNAL_STORAGE
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivity(intent)
                } else {
                    getRoot()
                    navigateToFragment()
                }
            }

        }
    }

    fun getShareViewModel(): ShareViewModel {
        return shareViewModel
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getRoot()
            navigateToFragment()
        } else {
            // Xử lý khi quyền bị từ chối (nếu cần)
        }
    }

    fun navigateToFragment() {
        val fragment = Fragment_FileManagement()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment).commit()
        button.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
        override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.addfolder -> {
                showCreateDialog(true)
                return true
            }

            R.id.addfile -> {
                showCreateDialog(false)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun showCreateDialog(isFolder: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isFolder) "Create Folder" else "Create File")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null)
        val editText = view.findViewById<EditText>(R.id.edt_name)
        builder.setView(view)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener { _, _ ->
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                // Gọi hàm để xử lý tạo thư mục hoặc file ở đây
                handleCreate(isFolder, name)
            } else {
                // Hiển thị thông báo lỗi khi tên rỗng
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        })

        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        })

        builder.show()
    }

    private fun handleCreate(isFolder: Boolean, name: String) {
        val currentPath = shareViewModel.currentPath.value

        val file = File(currentPath, name)

        if (isFolder) {
            // Xử lý tạo mới thư mục
            if (!file.exists() && file.mkdirs()) {
                // Thư mục đã được tạo thành công
                Toast.makeText(this, "Folder created successfully", Toast.LENGTH_SHORT).show()
                navigateToFragment()
            } else {
                // Có lỗi xảy ra khi tạo thư mục
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Xử lý tạo mới tệp (ví dụ: tạo file .txt)
            if (!file.exists() && file.createNewFile()) {
                // Tệp đã được tạo thành công
                Toast.makeText(this, "File created successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Có lỗi xảy ra khi tạo file
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onBackPressed() {
        val currentPath = shareViewModel.currentPath.value
        if (currentPath != Environment.getExternalStorageDirectory().path) {
            if (currentPath != null) {
                shareViewModel.currentPath.value = currentPath.substringBeforeLast("/")
                navigateToFragment()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun getRoot() {
        val rootDirectory = Environment.getExternalStorageDirectory().path
        shareViewModel.currentPath.value = rootDirectory
    }
}