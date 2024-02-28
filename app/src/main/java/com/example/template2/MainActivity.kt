package com.example.template2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()
    private var dataSnapshot: DataSnapshot? = null
    private var currentTaskKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = mAuth.currentUser
        val databaseReadReference =
            FirebaseDatabase.getInstance().reference.child("user")
                .child(currentUser?.uid ?: "default")
        val databaseWriteReference =
            FirebaseDatabase.getInstance().reference.child("user")
                .child(currentUser?.uid ?: "default")

        val listView = findViewById<ListView>(R.id.listView)
        val labelText: EditText = findViewById(R.id.labelText)
        val button = findViewById<Button>(R.id.buttonLabel)
        val todos: MutableList<String> = mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todos)
        listView.adapter = adapter

        databaseReadReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // Update dataSnapshot when data changes
                dataSnapshot = data
                todos.clear()
                for (taskSnapshot in data.children) {
                    val task = taskSnapshot.child("task").getValue(String::class.java)
                    if (task != null) {
                        todos.add(task)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка при загрузке заданий из Firebase",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


        button.setOnClickListener {
            val text = labelText.text.toString().trim()
            if (text.isNotEmpty()) {
                val newId = databaseWriteReference.push().key
                currentTaskKey = newId
                val taskMap = HashMap<String, Any>()
                taskMap["task"] = text
                if (newId != null) {
                    databaseWriteReference.child(newId).setValue(taskMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Задание добавлено в Firebase",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Ошибка при добавлении задания в Firebase",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
                labelText.text.clear()
            }
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val taskId = currentTaskKey
            val text = listView.getItemAtPosition(position).toString()

            if (taskId != null) {
                databaseWriteReference.child(taskId).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update UI only if the task removal is successful
                        todos.removeAt(position)
                        adapter.notifyDataSetChanged()
                        Toast.makeText(
                            this@MainActivity,
                            "Задание $text удалено из Firebase",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Ошибка при удалении задания из Firebase",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mAuth.signOut()
    }
}
