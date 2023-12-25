package com.example.quicklist

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColor

import androidx.recyclerview.widget.RecyclerView



class ToDoAdapter(private val context: Context) :
    RecyclerView.Adapter<ToDoAdapter.TodoViewHolder>()
{
    private val todos: MutableList<Todo> = mutableListOf()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TodoListPrefs", Context.MODE_PRIVATE)

    init {
        // Load data from SharedPreferences when the adapter is created
        loadTodoList()
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {

        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                /* resource = */ R.layout.item_todo,
                /* root = */ parent,
                /* attachToRoot = */ false
            )
        )


    }



    fun addTodo(todo: Todo) {
        todos.add(todo)
        saveTodoList()
        notifyItemInserted(todos.size-1)

    }


    fun deletedonetodos(){
        todos.removeAll { todo ->
            todo.isChecked
        }
        saveTodoList()
        notifyDataSetChanged()

    }

    fun toggleStrikethrough(holder: TodoViewHolder,tvTodoTitle: TextView, isChecked: Boolean){
        if (isChecked){

            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
            holder.itemView.apply { findViewById<CardView>(R.id.cardtask).setCardBackgroundColor(resources.getColor(R.color.addcolor)) }

        }
        else {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.apply { findViewById<CardView>(R.id.cardtask).setCardBackgroundColor(resources.getColor(R.color.backcolorlight)) }

        }
    }


    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {


        val curTodo = todos[position]
        holder.itemView.apply {

            findViewById<TextView>(R.id.tvToDoTitle).text = curTodo.title
            findViewById<CheckBox>(R.id.cbDone).isChecked = curTodo.isChecked

            toggleStrikethrough(holder,findViewById<TextView>(R.id.tvToDoTitle),curTodo.isChecked)
            findViewById<CheckBox>(R.id.cbDone).setOnCheckedChangeListener { _, isChecked ->
                toggleStrikethrough(holder,findViewById<TextView>(R.id.tvToDoTitle),isChecked)
                curTodo.isChecked = !curTodo.isChecked
                saveTodoList()
            }

        }
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom)
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    private fun saveTodoList() {
        val editor = sharedPreferences.edit()
        val serializedTodoList = todos.joinToString(separator = ";") {
            "${it.title},${it.isChecked}"
        }
        editor.putString("todoList", serializedTodoList)
        editor.apply()
    }

    private fun loadTodoList() {
        val serializedTodoList = sharedPreferences.getString("todoList", "")
        todos.clear()

        if (!serializedTodoList.isNullOrBlank()) {
            val items = serializedTodoList.split(";")
            for (item in items) {
                val parts = item.split(",")
                if (parts.size == 2) {
                    val title = parts[0]
                    val completed = parts[1].toBoolean()
                    val todoItem = Todo(title, completed)
                    todos.add(todoItem)
                }
            }
        }
    }


}