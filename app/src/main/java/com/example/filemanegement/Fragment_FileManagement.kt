package com.example.filemanegement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class Fragment_FileManagement : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_management, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val shareViewModel = (requireActivity() as MainActivity).getShareViewModel()
        // Lấy thư mục gốc
        val rootDirectory = File(shareViewModel.currentPath.value)
        val folderItem: FileItem
        if (rootDirectory.exists() && rootDirectory.isDirectory) {
            folderItem = FileItem(
                rootDirectory.name,
                rootDirectory.path,
                true,
                mutableListOf()
            )
            listFiles(folderItem)
        }else{
            folderItem = FileItem(
                "root",
                "/",
                true,
                mutableListOf()
            )
        }

        // Tạo và cấu hình adapter
        val adapterFileManagement = Adapter_FileManagement(folderItem.listFile ?: mutableListOf())
        recyclerView.adapter = adapterFileManagement
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view
    }

    // Hàm đệ quy để lặp qua tất cả các thư mục con và lấy tất cả các tệp
    private fun listFiles(folderItem: FileItem) {
        val directory = File(folderItem.path)
        val filesAndFolders: Array<File>? = directory.listFiles()
        if (filesAndFolders != null) {
        }else{
        }
        if (filesAndFolders != null) {
            for (fileOrFolder in filesAndFolders) {
                if (fileOrFolder.isDirectory) {
                    val subFolder =
                        FileItem(fileOrFolder.name, fileOrFolder.path, true, mutableListOf())
                    folderItem.listFile?.add(subFolder)
                    listFiles(subFolder)
                } else {
                    folderItem.listFile?.add(FileItem(fileOrFolder.name, fileOrFolder.path, false))
                }
            }
        }
    }
}