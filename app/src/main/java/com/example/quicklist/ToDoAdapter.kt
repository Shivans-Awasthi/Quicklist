package com.example.quicklist

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.os.Build
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button

import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColor

import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Filter out the tasks to delete
        val tasksToDelete = todos.filter { it.isChecked }

        // Cancel notifications for each task to delete
        tasksToDelete.forEach { todo ->
            notificationManager.cancel(todo.notifid)
            Log.d("NotificationDeleted", "Notification ID: ${todo.notifid}")
        }

        // Remove the tasks from the list
        todos.removeAll(tasksToDelete)


        // Save and update the RecyclerView
        saveTodoList()
        notifyDataSetChanged()


    }

    fun sortByTaskTime() {
        todos.sortBy { it.tasktime }
        notifyDataSetChanged()
    }



    fun toggleStrikethrough(holder: TodoViewHolder, tvTodoTitle: TextView, tvtasktime: TextView, tvTodoCat: TextView, isChecked: Boolean) {
        val context = holder.itemView.context  // Get the context from the itemView

        if (isChecked) {
            // Fade out animation
            val fadeOut = ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0.5f)
            fadeOut.duration = 200
            fadeOut.start()

            // Apply strikethrough after the fade out
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
                    tvtasktime.paintFlags = tvtasktime.paintFlags or STRIKE_THRU_TEXT_FLAG
                    tvTodoCat.paintFlags = tvtasktime.paintFlags or STRIKE_THRU_TEXT_FLAG
                }
            })

            // Background color transition
            val deleteColor = context.resources.getColor(R.color.deletecolor)
            val colorTransition = ObjectAnimator.ofObject(
                holder.itemView.findViewById<CardView>(R.id.cardtask).backgroundTintList,
                "tint",
                ArgbEvaluator(),
                deleteColor,
                deleteColor
            )
            colorTransition.duration = 200
            colorTransition.start()
        } else {
            // Remove strikethrough
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            tvtasktime.paintFlags = tvtasktime.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            tvTodoCat.paintFlags = tvtasktime.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()

            // Background color transition
            val backColor = context.resources.getColor(R.color.backcolor)
            val colorTransition = ObjectAnimator.ofObject(
                holder.itemView.findViewById<CardView>(R.id.cardtask).backgroundTintList,
                "tint",
                ArgbEvaluator(),
                backColor,
                backColor
            )
            colorTransition.duration = 200
            colorTransition.start()

            // Fade in animation
            val fadeIn = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0.5f, 1f)
            fadeIn.duration = 200
            fadeIn.start()
        }
    }




    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {

        lateinit var arrow: ImageButton
        lateinit var hiddenView: TextView
        lateinit var cardView: CardView


        val curTodo = todos[position]

        holder.itemView.apply {

            findViewById<TextView>(R.id.tvToDoTitle).text = curTodo.title
            findViewById<TextView>(R.id.tvToDoDetails).text = curTodo.taskdetail
            findViewById<TextView>(R.id.tvtasktime).text = SimpleDateFormat(
                "(dd MMM, hh:mm a)",
                Locale.getDefault()
            ).format(Date(curTodo.tasktime))
            findViewById<CheckBox>(R.id.cbDone).isChecked = curTodo.isChecked
            findViewById<TextView>(R.id.tvToDoCat).text = curTodo.category


            if (curTodo.category == "Work") {
                findViewById<CardView>(R.id.colorcard).setCardBackgroundColor(resources.getColor(R.color.workcolor))
            } else if (curTodo.category == "Home") {
                findViewById<CardView>(R.id.colorcard).setCardBackgroundColor(resources.getColor(R.color.homecolor))
            } else {
                findViewById<CardView>(R.id.colorcard).setCardBackgroundColor(resources.getColor(R.color.addcolor2))
            }


            toggleStrikethrough(
                holder,
                findViewById(R.id.tvToDoTitle),
                findViewById(R.id.tvtasktime),
                findViewById(R.id.tvToDoCat),
                curTodo.isChecked
            )
            findViewById<CheckBox>(R.id.cbDone).setOnCheckedChangeListener { _, isChecked ->
                toggleStrikethrough(
                    holder,
                    findViewById(R.id.tvToDoTitle),
                    findViewById(R.id.tvtasktime),
                    findViewById(R.id.tvToDoCat),
                    isChecked
                )
                curTodo.isChecked = !curTodo.isChecked
                saveTodoList()
            }

//            details.animation = AnimationUtils.loadAnimation(
//                details.context,
//                androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom
//            )

            cardView = findViewById(R.id.cardtask)
            arrow = findViewById(R.id.btndetailexpand)
            hiddenView = findViewById(R.id.tvToDoDetails)

            hiddenView.visibility = View.GONE


            findViewById<ImageButton>(R.id.btndetailexpand).setOnClickListener {
//                if(details.visibility == View.GONE)
//                {
//                    details.visibility = View.VISIBLE
//                    TransitionManager.beginDelayedTransition(cardView, new AutoTransition());
//                    hiddenView.setVisibility(View.GONE);
//                    arrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
//                }
//                else details.visibility = View.GONE

//            }

                if (hiddenView.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                    hiddenView.visibility = View.GONE
                    arrow.setImageResource(R.drawable.expand)

                } else {
                    TransitionManager.beginDelayedTransition(cardView, AutoTransition() )
                    hiddenView.visibility = View.VISIBLE
                    arrow.setImageResource(R.drawable.expand_less)
                }


            }

            holder.itemView.animation = AnimationUtils.loadAnimation(
                holder.itemView.context,
                androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom
            )
        }
    }



        override fun getItemCount(): Int {
            return todos.size
        }


        private fun saveTodoList() {
            val editor = sharedPreferences.edit()
            val serializedTodoList = todos.joinToString(separator = ";") {
                "${it.title},${it.tasktime},${it.taskdetail},${it.notifid},${it.category},${it.isChecked}"
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
                    if (parts.size == 6) {
                        val title = parts[0]
                        val time = parts[1].toLong()
                        val detail = parts[2].toString()
                        val notid = parts[3].toInt()
                        val cat = parts[4].toString()
                        val completed = parts[5].toBoolean()
                        val todoItem = Todo(title, time, detail, notid, cat, completed)
                        todos.add(todoItem)
                    }
                }
            }
        }

    }


