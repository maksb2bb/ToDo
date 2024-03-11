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

    private lateinit var mAuth: FirebaseAuth
    private var dataSnapshot: DataSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        // Проверяем, аутентифицирован ли пользователь
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
            finish() // Закрыть активность, если пользователь не аутентифицирован
            return
        }

        val userUid = currentUser.uid // Теперь безопасно использовать UID
        val databaseReference = FirebaseDatabase.getInstance().reference.child("user").child(userUid)

        val listView: ListView = findViewById(R.id.listView)
        val labelText: EditText = findViewById(R.id.labelText)
        val button: Button = findViewById(R.id.buttonLabel)
        val todos: MutableList<String> = mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todos)
        listView.adapter = adapter

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                dataSnapshot = data
                todos.clear()
                data.children.forEach { taskSnapshot ->
                    taskSnapshot.child("task").getValue(String::class.java)?.let { task ->
                        todos.add(task)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Ошибка при загрузке заданий: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        button.setOnClickListener {
            val text = labelText.text.toString().trim()
            if (text.isNotEmpty()) {
                val newId = databaseReference.push().key ?: return@setOnClickListener // Возврат, если ключ null
                val taskMap = hashMapOf("task" to text)
                databaseReference.child(newId).setValue(taskMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Задание добавлено", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Ошибка при добавлении задания: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                labelText.text.clear()
            } else {
                Toast.makeText(this, "Задание не может быть пустым", Toast.LENGTH_SHORT).show()
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            dataSnapshot?.children?.elementAt(position)?.key?.let { taskKey ->
                databaseReference.child(taskKey).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Задание удалено", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Ошибка при удалении задания: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuth.signOut() // Выход из учетной записи при уничтожении активности
    }
}
