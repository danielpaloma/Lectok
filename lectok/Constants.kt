package com.lectok

object Constants {
    const val DB_NAME = "BIBLIOTECA"
    const val DB_VERSION = 1
    const val TABLE_NAME = "BOOKS_TABLE"
    //columns
    const val C_ID = "ID"
    const val C_TITLE = "TITLE"
    const val C_AUTHOR = "AUTHOR"
    const val C_IMAGE = "IMAGE"
    const val C_TOTAL_PAGES = "TOTAL_PAGES"
    const val C_READ_PAGES = "READ_PAGES"
    const val C_ADDED_TIMESTAMP = "ADDED_TIMESTAMP"
    const val C_UPDATED_TIMESTAMP = "UPDATED_TIMESTAMP"

    //create table query
    const val CREATE_TABLE = (
            "CREATE TABLE " + TABLE_NAME + "("
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + C_TITLE + " TEXT,"
            + C_AUTHOR + " TEXT,"
            + C_IMAGE + " TEXT,"
            + C_TOTAL_PAGES + " TEXT,"
            + C_READ_PAGES + " TEXT,"
            + C_ADDED_TIMESTAMP + " TEXT,"
            + C_UPDATED_TIMESTAMP + " TEXT"
            + ")"
            )

}