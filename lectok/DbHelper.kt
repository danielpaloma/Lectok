package com.lectok

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//MyDbHelper : database helper class that contains all CRUD methods

class DbHelper(context: Context?): SQLiteOpenHelper(
    context,
    Constants.DB_NAME,
    null,
    Constants.DB_VERSION

) {
    override fun onCreate(db: SQLiteDatabase) {
        //create table on that DB
        db.execSQL(Constants.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion:Int, newVersion: Int) {
        //upgrade database (if there is any structure change, change db version)
        //drop older table if exists

        db.execSQL("DROP TABLE IF EXISTS" + Constants.TABLE_NAME)
        onCreate(db)
    }

    //insert record to DB
    fun insertRecord(
        title:String?,
        author:String?,
        image:String?,
        totalPages:String?,
        readPages:String?,
        addedTimestamp:String,
        updatedTimestamp:String?
    ):Long {
        //get writable database
        val db = this.writableDatabase
        val values = ContentValues()

        //insert data
        values.put(Constants.C_TITLE, title)
        values.put(Constants.C_AUTHOR, author)
        values.put(Constants.C_IMAGE, image)
        values.put(Constants.C_TOTAL_PAGES, totalPages)
        values.put(Constants.C_READ_PAGES, readPages)
        values.put(Constants.C_ADDED_TIMESTAMP, addedTimestamp)
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTimestamp)

        //insert row
        val id = db.insert(Constants.TABLE_NAME, null, values)

        db.close()

        return id
    }

    //update record to db
    fun updateRecord(   id:String,
                        title: String?,
                        author: String?,
                        image: String?,
                        totalPages: String?,
                        readPages: String?,
                        addedTimestamp: String?,
                        updatedTimestamp: String?
                    ):Long {
        //get writable database
        val db = this.writableDatabase
        val values = ContentValues()

        //insert data
        values.put(Constants.C_TITLE, title)
        values.put(Constants.C_AUTHOR, author)
        values.put(Constants.C_IMAGE, image)
        values.put(Constants.C_TOTAL_PAGES, totalPages)
        values.put(Constants.C_READ_PAGES, readPages)
        values.put(Constants.C_ADDED_TIMESTAMP, addedTimestamp)
        values.put(Constants.C_UPDATED_TIMESTAMP, updatedTimestamp)

        return db.update(Constants.TABLE_NAME,
            values,
            "${Constants.C_ID}=?",
            arrayOf(id)).toLong()
    }


    //GET ALL DATA
    fun getAllRecords(orderBy:String):ArrayList<ModelRecord>{

        val recordList = ArrayList<ModelRecord>()
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME} ORDER BY $orderBy"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                val modelRecord = ModelRecord(
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ID)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_TITLE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_AUTHOR)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_TOTAL_PAGES)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_READ_PAGES)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                )

                //add record to list
                recordList.add(modelRecord)
            }while (cursor.moveToNext())
        }

        db.close()

        return recordList
    }

    //search data
    fun searchRecords(query:String):ArrayList<ModelRecord>{


        val recordList = ArrayList<ModelRecord>()
        val selectQuery = "SELECT * FROM ${Constants.TABLE_NAME} WHERE ${Constants.C_TITLE} LIKE '%$query%'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()){
            do {
                val modelRecord = ModelRecord(
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ID)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_TITLE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_AUTHOR)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_IMAGE)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_TOTAL_PAGES)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_READ_PAGES)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_ADDED_TIMESTAMP)),
                    ""+cursor.getString(cursor.getColumnIndex(Constants.C_UPDATED_TIMESTAMP))
                )

                //add record to list
                recordList.add(modelRecord)
            }while (cursor.moveToNext())
        }

        db.close()

        return recordList
    }

    //PART 4
    //get total number of records
    fun recordCount():Int{
        val countQuery = "SELECT * FROM ${Constants.TABLE_NAME}"
        val db = this.readableDatabase
        val cursor = db.rawQuery(countQuery,null)
        val count = cursor.count
        cursor.close()
        return count
    }

    //delete (single) record using record id
    fun deleteRecord(id: String){
        val db = writableDatabase
        db.delete(
            Constants.TABLE_NAME,
            "${Constants.C_ID} = ?",
            arrayOf(id)
        )
        db.close()
    }

    //delete all records
    fun deleteAllRecords(){
        val db = writableDatabase
        db.execSQL("DELETE FROM ${Constants.TABLE_NAME}")
        db.close()
    }
}