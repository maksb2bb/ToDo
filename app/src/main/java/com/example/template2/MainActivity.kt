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
        //FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        val listView = findViewById<ListView>(R.id.listView)
        val labelText: EditText = findViewById(R.id.labelText)
        val button = findViewById<Button>(R.id.buttonLabel)
        val todos: MutableList<String> = mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todos)
        listView.adapter = adapter

        databaseReadReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
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
                Toast.makeText(
                    this@MainActivity,
                    "Ошибка при загрузке заданий",
                    Toast.LENGTH_SHORT
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
                                    "Задание добавлено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Ошибка при добавлении задания",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
                labelText.text.clear()
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            // Get the task key from the selected position
            val taskKey = dataSnapshot?.children?.toList()?.get(position)?.key

            // Check if the task key is not null
            if (taskKey != null) {
                // Remove the task from the list
                todos.removeAt(position)
                adapter.notifyDataSetChanged()

                // Remove the task from the Firebase database
                databaseWriteReference.child(taskKey).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Задание удалено",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Ошибка при удалении задания",
                                Toast.LENGTH_SHORT
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
