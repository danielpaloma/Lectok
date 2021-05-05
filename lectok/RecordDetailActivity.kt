package com.lectok


import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.ActionBar
import kotlinx.android.synthetic.main.activity_record_detail.*
import java.util.*

class RecordDetailActivity : AppCompatActivity() {

    //action bar
    private var actionBar: ActionBar?=null

    //db helper
    private var dbHelper:DbHelper?=null
    private var recordId:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)

        //setting up actionbar
        actionBar = supportActionBar
        actionBar!!.title = "Record Details"
        actionBar!!.setDisplayShowHomeEnabled(true)
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        dbHelper = DbHelper(this)

        //get record id from intent
        val intent = intent
        recordId = intent.getStringExtra("RECORD_ID") //from AdapterRecord.kt

        showRecordDetails()

    }

    private fun showRecordDetails() {
        //get record details
        val selectQuery =
            """SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_ID} ="$recordId""""
        val db = dbHelper!!.writableDatabase

        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do{
                val id = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ID))
                val title = ""+cursor.getString(cursor.getColumnIndex(Constants.C_TITLE))
                val author = ""+cursor.getString(cursor.getColumnIndex(Constants.C_AUTHOR))
                val image = ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE))
                val totalpages = ""+cursor.getString(cursor.getColumnIndex(Constants.C_TOTAL_PAGES))
                val readpages = ""+cursor.getString(cursor.getColumnIndex(Constants.C_READ_PAGES))
                val addedTimestamp = ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP))
                val updatedTimestamp =""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))

                //convert timestamp
                val calendar1 = Calendar.getInstance(Locale.getDefault())
                calendar1.timeInMillis = addedTimestamp.toLong()
                val timeAdded = DateFormat.format("dd/MM/yyyy", calendar1)

                val calendar2 = Calendar.getInstance(Locale.getDefault())
                calendar2.timeInMillis = updatedTimestamp.toLong()
                val timeUpdated = DateFormat.format("dd/MM/yyyy", calendar2)

                //set data
                titleTv.text = title
                authorTv.text = author
                totalpagesTv.text = totalpages
                readpagesTv.text = readpages

                val progress = (readpages.toFloat() / totalpages.toFloat()) * 100
                progressTv.text = "%.2f".format(progress)

                addedDateTv.text = timeAdded
                updatedDateTv.text = timeUpdated

                //Image of cover book
                if (image == "null") {
                    book_cover_iv.setImageResource(R.drawable.ic_book)
                }
                else{
                    book_cover_iv.setImageURI(Uri.parse(image))
                }

            } while (cursor.moveToNext())
        }

        db.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}