package com.lectok

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class MainActivity : AppCompatActivity() {

    //db helper
    lateinit var dbHelper:DbHelper

    //orderby /sort queries
    private val NEWEST_FIRST = "${Constants.C_ADDED_TIMESTAMP} DESC"
    private val OLDEST_FIRST = "${Constants.C_ADDED_TIMESTAMP} ASC"
    private val TITLE_ASC = "${Constants.C_TITLE} ASC"
    private val TITLE_DESC = "${Constants.C_TITLE} DESC"

    private var recentSortOrder = NEWEST_FIRST

    //PART 5: BACKUP AND RESTORE RECORDS
    // ----for storage permission
    private val STORAGE_REQUEST_CODE_EXPORT = 1
    private val STORAGE_REQUEST_CODE_IMPORT = 2
    private lateinit var storagePermission:Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //P5: init array of permission
        storagePermission = arrayOf(WRITE_EXTERNAL_STORAGE)

        //init db helper
        dbHelper = DbHelper(this)

        loadRecords(NEWEST_FIRST) //By default order by newest

        //click FloatinActionButton to start AddUpdateRecordActivity
        //addRecordBtn from activity_main.xml
        addRecordBtn.setOnClickListener {
            val intent = Intent(this, AddUpdateRecordActivity::class.java)
            intent.putExtra("isEditMode", false) //want to add new record, set it false
            startActivity(intent)
        }
    }

    private fun checkStoragePermission():Boolean{
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermissionImport(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_IMPORT)
    }

    private fun requestStoragePermissionExport(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_EXPORT)
    }

    fun exportCSV(){
        //path of csv file


        val folder = File("${Environment.getExternalStorageDirectory()}/SQLiteBackupKotlin")

        var isFolderCreated = false
        if (!folder.exists()) isFolderCreated = folder.mkdir()

        val csvFileName = "SQLiteBackup.csv"

        val fileNameAndPath = "$folder/$csvFileName"

        //get records to save in backup
        var recordList = ArrayList<ModelRecord>()
        recordList.clear()
        recordList = dbHelper.getAllRecords(OLDEST_FIRST)

        try{
            val fw = FileWriter(fileNameAndPath)
            for (i in recordList.indices){
                fw.append(""+recordList[i].id)
                fw.append(",")
                fw.append(""+recordList[i].title)
                fw.append(",")
                fw.append(""+recordList[i].author)
                fw.append(",")
                fw.append(""+recordList[i].image)
                fw.append(",")
                fw.append(""+recordList[i].totalpages)
                fw.append(",")
                fw.append(""+recordList[i].readpages)
                fw.append(",")
                fw.append(""+recordList[i].addedTime)
                fw.append(",")
                fw.append(""+recordList[i].updatedTime)
                fw.append("\n")
            }
            fw.flush()
            fw.close()

            Toast.makeText(this, "Backup Exported to $fileNameAndPath ...", Toast.LENGTH_SHORT).show()


        } catch (e: Exception){
            Toast.makeText(this, e.message , Toast.LENGTH_SHORT).show()

        }
    }

    fun importCSV(){
        //complete path to csv file
        val filePathAndName = "${Environment.getExternalStorageDirectory()}/SQLiteBackupKotlin/SQLiteBackup.csv"

        val csvFile = File(filePathAndName)
        //check if backup file exists
        if (csvFile.exists()){
            try{
                val csvReader = CSVReader(FileReader(csvFile.absolutePath))
                var nextLine:Array<String>
                while (csvReader.readNext().also {nextLine = it} != null){

                    //get record from CSV
                    val idd = nextLine[0]
                    val title = nextLine[1]
                    val author = nextLine[2]
                    val image = nextLine[3]
                    val totalpages = nextLine[4]
                    val readpages = nextLine[5]
                    val addedTime = nextLine[6]
                    val updatedTime = nextLine[7]

                    //add to db
                    val timestamp = System.currentTimeMillis()
                    val id = dbHelper.insertRecord(
                        ""+title,
                        ""+author,
                        ""+image,
                        ""+totalpages,
                        ""+readpages,
                        "$timestamp",
                        "$timestamp"
                    )
                }
            } catch (e: Exception){
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "Backup not found", Toast.LENGTH_SHORT).show()

        }

    }

    private fun loadRecords(orderBy:String) {
        recentSortOrder = orderBy;
        val adapterRecord = AdapterRecord(this, dbHelper.getAllRecords(orderBy))

        recordsRv.adapter = adapterRecord
    }

    private fun searchRecords(query:String) {
        val adapterRecord = AdapterRecord(this, dbHelper.searchRecords(query))

        recordsRv.adapter = adapterRecord
    }

    private fun sortDialog() {
        //OPTIONS to display in dialog
        val options = arrayOf("Title Ascending", "Title Descending", "Newest", "Oldest")
        //dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Sort By")

            .setItems(options){ _, which ->
                //handle items
                if (which == 0){
                    //title ascending
                    loadRecords(TITLE_ASC)
                }
                else if(which ==1){
                    //title descending
                    loadRecords(TITLE_DESC)
                }
                else if (which == 2){
                    //newest
                    loadRecords(NEWEST_FIRST)
                }
                else if (which == 3){
                    //oldest
                    loadRecords(OLDEST_FIRST)
                }

            }.show()
    }


    public override fun onResume() {
        super.onResume()
        loadRecords(recentSortOrder)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //inflate menu
        menuInflater.inflate(R.menu.menu_main, menu)

        //search view
        val item = menu.findItem(R.id.action_search)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                //search as you type
                if (newText != null) {
                    searchRecords(newText)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                //search when search button on keyboard is clicked
                if (query != null) {
                    searchRecords(query)
                }
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handle menu item clicks
        val id = item.itemId

        if(id ==R.id.action_sort){
            sortDialog()
        }
        /*else if (id == R.id.action_deleteall){
            //delete all records
            dbHelper.deleteAllRecords()
            onResume()
        }*/
        else if(id == R.id.action_backup){
            //back up all records
            if (checkStoragePermission()){
                exportCSV()
            }
            else {
                requestStoragePermissionExport()
            }

        }
        else if(id == R.id.action_restore){
            //restore all records
            if (checkStoragePermission()){
                importCSV()
                onResume()
            }
            else {
                requestStoragePermissionImport()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            STORAGE_REQUEST_CODE_EXPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    exportCSV()
                }
                else{
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_REQUEST_CODE_IMPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    importCSV()
                }
                else{
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }


}