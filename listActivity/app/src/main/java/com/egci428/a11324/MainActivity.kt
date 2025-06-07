package com.egci428.a11324

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.listactivity.R

class MainActivity : AppCompatActivity() {
    private var data = ArrayList<FortuneCookie>()
    private lateinit var adapter: CourseArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView = findViewById<ListView>(R.id.listView)

        // Load saved data, then setup adapter
        DataProvider.loadFromFile(this)
        adapter = CourseArrayAdapter(this, 0, data)
        listView.adapter = adapter

        val addButton = findViewById<ImageView>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            startActivity(intent)
        }

        // This the DELETING FUNCTION
        listView.setOnItemLongClickListener { parent, view, position, id ->
            if (position !in 0 until data.size) return@setOnItemLongClickListener false

            DataProvider.deleteItem(position, this)
            data.removeAt(position)
            adapter.notifyDataSetChanged()

            // Animation as required in the handout
            view.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    listView.smoothScrollToPosition(Math.min(position, adapter.count - 1))
                }
                .start()

            true
        }
    }

    override fun onResume() {
        super.onResume()
        DataProvider.loadFromFile(this)
        data.clear()
        data.addAll(DataProvider.getData())
        adapter.notifyDataSetChanged()
    }

    private fun sendDetail(cookie: FortuneCookie) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("message", cookie.message)
        intent.putExtra("status", cookie.status)
        startActivity(intent)
    }

    private class CourseArrayAdapter(var context: Context, resource: Int, var objects: ArrayList<FortuneCookie>) : BaseAdapter() {
        override fun getCount(): Int = objects.size
        override fun getItem(position: Int): Any = objects[position]
        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
            val aRow = convertView ?: LayoutInflater.from(viewGroup!!.context)
                .inflate(R.layout.course_row, viewGroup, false).apply {
                    tag = ViewHolder(
                        findViewById(R.id.titleTextView),
                        findViewById(R.id.imageView),
                        findViewById(R.id.timeTextView)
                    )
                }

            val viewHolder = aRow.tag as ViewHolder
            val cookie = objects[position]

            viewHolder.titleTextView.text = cookie.message
            viewHolder.timeTextView.text = cookie.time
            viewHolder.titleTextView.setTextColor(
                if (cookie.status == "positive") Color.parseColor("#FFA500") else Color.BLUE
            )

            val res = context.resources.getIdentifier(
                "fortunecookieicon",
                "drawable",
                context.packageName
            )
            viewHolder.imageView.setImageResource(res)

            return aRow
        }

        private class ViewHolder(
            val titleTextView: TextView,
            val imageView: ImageView,
            val timeTextView: TextView
        )
    }
}