package com.lectok

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Adapter class for recycler view
class AdapterRecord(): RecyclerView.Adapter<AdapterRecord.HolderRecord>() {

    private var context: Context? = null
    private var recordList: ArrayList<ModelRecord>? = null

    lateinit var dbHelper:DbHelper

    constructor(context:Context?, recordList:ArrayList<ModelRecord>?) : this() {
        this.context = context
        this.recordList = recordList

        dbHelper = DbHelper(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecord {

        //inflate the layout row_record.xml
        return  HolderRecord(
            LayoutInflater.from(context).inflate(R.layout.row_record, parent, false)
        )
    }

    override fun getItemCount(): Int {

        //return items/records/list size
        return recordList!!.size
    }

    override fun onBindViewHolder(holder: HolderRecord, position: Int) {

        //get data, set data handle clicks

        //get data
        val model = recordList!!.get(position)

        val id = model.id
        val title = model.title
        val author = model.author
        val image = model.image
        val totalpages = model.totalpages
        val readpages = model.readpages
        val addedTime = model.addedTime
        val updatedTime = model.updatedTime

        //set data to views
        holder.titleTv.text = title
        holder.authorTv.text = author
        holder.totalpagesTv.text = totalpages
        holder.readpagesTv.text = readpages

        //Calculate progress and show in row_record.xml
        val progress = (readpages.toFloat() / totalpages.toFloat()) * 100
        holder.progressTv.text = "%.2f".format(progress)
        fun validate_progress(){
            TODO("Validate input data to calculate progress, " +
                    "e.g. pages are float, numeric data, progress <= 100%")
        }


        //Image of cover book
        if (image == "null") {
            holder.book_cover_iv.setImageResource(R.drawable.ic_book)
        }
        else{
            holder.book_cover_iv.setImageURI(Uri.parse(image))
        }

        //show record on new activity on clicking record
        holder.itemView.setOnClickListener{
            //pass id to next activity to show record
            val intent = Intent(context, RecordDetailActivity::class.java)
            intent.putExtra("RECORD_ID", id)
            context!!.startActivity(intent)
        }

        //handle more button click: show delete/edit options
        holder.moreBtn.setOnClickListener {
            showMoreOptions(
                position,
                id,
                title,
                author,
                image,
                totalpages,
                readpages,
                addedTime,
                updatedTime
            )
        }

    }

    private fun showMoreOptions(
        position: Int,
        id: String,
        title: String,
        author: String,
        image: String,
        totalpages: String,
        readpages: String,
        addedTime: String,
        updatedTime: String
    ) {
        //options to display in dialog
        val options = arrayOf("Edit","Delete")
        val dialog : AlertDialog.Builder = AlertDialog.Builder(context)

        //set items and click listener
        dialog.setItems(options) { dialog, which ->
            //handle item clicks
            if (which == 0){
                //edit clicked
                val intent = Intent(context, AddUpdateRecordActivity::class.java)
                intent.putExtra("ID",id)
                intent.putExtra("TITLE",title)
                intent.putExtra("AUTHOR",author)
                intent.putExtra("IMAGE", image)
                intent.putExtra("TOTALPAGES", totalpages)
                intent.putExtra("READPAGES", readpages)
                intent.putExtra("ADDED",addedTime)
                intent.putExtra("UPDATED",updatedTime)
                intent.putExtra("isEditMode", true)
                context!!.startActivity(intent)
            } else {
                //delete clicked
                dbHelper.deleteRecord(id)
                //refresh record by calling activity's onResume method
                (context as MainActivity)!!.onResume();
            }

        }

        //show dialog
        dialog.show()

    }

    inner class HolderRecord(itemView: View):RecyclerView.ViewHolder(itemView){

        //views from row_record.xml
        var book_cover_iv:ImageView = itemView.findViewById(R.id.book_cover_iv)
        var titleTv: TextView = itemView.findViewById(R.id.titleTv)
        var authorTv: TextView = itemView.findViewById(R.id.authorTv)
        var totalpagesTv: TextView = itemView.findViewById(R.id.totalpagesTv)
        var readpagesTv: TextView = itemView.findViewById(R.id.readpagesTv)
        var progressTv: TextView = itemView.findViewById(R.id.progressTv)
        var moreBtn: ImageButton = itemView.findViewById(R.id.moreBtn)

    }


}