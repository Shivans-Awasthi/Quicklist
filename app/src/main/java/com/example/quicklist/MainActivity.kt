package com.example.quicklist

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.CalendarContract.CalendarAlerts
import android.text.Editable
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quicklist.databinding.ActivityMainBinding
import com.example.quicklist.databinding.AddTaskBinding
import com.example.quicklist.databinding.AddTodoTaskBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.hypot
import kotlin.properties.Delegates
import android.util.AttributeSet
import android.widget.DatePicker
import androidx.appcompat.widget.AppCompatEditText

class MainActivity : AppCompatActivity() {

    lateinit var toDoAdapter: ToDoAdapter
    private lateinit var binding: ActivityMainBinding

    var calendar = Calendar.getInstance()
    var todocount = 0

    private val buttonColorsMap = HashMap<Button, ColorStateList>()

    private var notifcounter = 0
    private var timeflag = 0

    var notiftime: Long = 0

    var lastpressedButton: AppCompatButton? = null

    private fun scheduleNotification(timeInMillis: Long, message: String, notifid: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("message", message)
        intent.putExtra("id", notifid)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            notifid,
            intent,
            flags
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

    }



    var todocategory = "General"


    private fun updateEmptyStateVisibility() {
        if (toDoAdapter.itemCount == 0) {
            binding.rvToDoItems.visibility = View.GONE
            binding.emptyStateImage.visibility = View.VISIBLE
            binding.emptyStateImage.animation = AnimationUtils.loadAnimation(binding.emptyStateImage.context,androidx.appcompat.R.anim.abc_fade_in)
        } else {
            binding.rvToDoItems.visibility = View.VISIBLE
            binding.emptyStateImage.visibility = View.GONE

        }
    }


    private fun addtaskfunc() {




        val dialogView = layoutInflater.inflate(R.layout.add_todo_task, null)


        val popupWindow = PopupWindow(
            dialogView,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        findViewById<AppCompatButton>(R.id.add).visibility = View.GONE

        popupWindow.animationStyle = R.style.PopupAnimation

        var etTaskTitle = dialogView.findViewById<EditText>(R.id.etToDoTitle)
        var etTaskDetails = dialogView.findViewById<EditText>(R.id.etToDoDetails)
        val btnAddTask = dialogView.findViewById<Button>(R.id.btnAddToDo)
        val selecttime: AppCompatButton = dialogView.findViewById(R.id.btnselecttime)
        val closebtn = dialogView.findViewById<ImageButton>(R.id.closebtn)
        val workbtn = dialogView.findViewById<AppCompatButton>(R.id.btnwork)
        val homebtn = dialogView.findViewById<AppCompatButton>(R.id.btnhome)


        buttonColorsMap[workbtn] = ColorStateList.valueOf(resources.getColor(R.color.workcolor))
        buttonColorsMap[homebtn] = ColorStateList.valueOf(resources.getColor(R.color.homecolor))

        popupWindow.isFocusable = true
        etTaskTitle.requestFocus()

        btnAddTask.setOnClickListener {

            if (etTaskTitle.text.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()

            } else if (timeflag == 0) {
                Toast.makeText(this, "Select the time", Toast.LENGTH_SHORT).show()
            } else {

                var todoTitle = etTaskTitle.text.toString()
                var todoDetails = etTaskDetails.text.toString()
                if (todoTitle.isNotEmpty()) {
                    val notificationId = notifcounter++
                    val todo = Todo(todoTitle, notiftime, todoDetails, notificationId, todocategory)
                    toDoAdapter.addTodo(todo)
                    etTaskTitle.text.clear()

                    toDoAdapter.sortByTaskTime()

                    scheduleNotification(todo.tasktime, todoTitle, todo.notifid)

                    Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show()
                    todocount = toDoAdapter.itemCount
                    binding.tvnooftask.text = "${todocount} Task Pending"
                    updateEmptyStateVisibility()
                    popupWindow.dismiss()

                }
            }
        }

        popupWindow.setOnDismissListener {
            findViewById<AppCompatButton>(R.id.add).visibility = View.VISIBLE


        }

        closebtn.setOnClickListener {

            popupWindow.dismiss()

        }


        workbtn.setOnClickListener {
            onButtonClicked(workbtn)
        }

        homebtn.setOnClickListener {
            onButtonClicked(homebtn)
        }


        selecttime.setOnClickListener {

            showDatePickerDialog(selecttime)

        }
        popupWindow.showAtLocation(dialogView, Gravity.BOTTOM, 0, 0)
    }

    private fun showtimePickerDialog(selecttime:AppCompatButton) {
        val dialogViewtime = layoutInflater.inflate(R.layout.custom_timepicker, null)
        val btnSetTime = dialogViewtime.findViewById<AppCompatButton>(R.id.btnSetTime)
        val timePicker = dialogViewtime.findViewById<TimePicker>(R.id.timePicker)


        val timePickerDialog = AlertDialog.Builder(this)
            .setView(dialogViewtime)
            .create()

        btnSetTime.setOnClickListener {

            val hourOfDay = timePicker.hour
            val minute = timePicker.minute
//            calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

//            if (calendar.timeInMillis < System.currentTimeMillis()) {
//                Toast.makeText(this, "Time is in the past", Toast.LENGTH_SHORT).show()
//            }
//            else {

                notiftime = calendar.timeInMillis
                timePickerDialog.dismiss()
                selecttime.text = SimpleDateFormat("dd MMM, hh:mm a",Locale.getDefault()).format(Date(notiftime)).toString()

//            }
        }


        timePickerDialog.show()
        timeflag = 1

    }



    private fun showDatePickerDialog(selecttime:AppCompatButton) {

        val dialogViewtime = layoutInflater.inflate(R.layout.custom_datepicker, null)
        val btnSetTime = dialogViewtime.findViewById<AppCompatButton>(R.id.btnSetDate)
        val DatePicker = dialogViewtime.findViewById<DatePicker>(R.id.DatePicker)


        val datePickerDialog = AlertDialog.Builder(this)
            .setView(dialogViewtime)
            .setIcon(R.drawable.calendar)
            .create()

        btnSetTime.setOnClickListener {

            val dayofmonth = DatePicker.dayOfMonth
            val month = DatePicker.month
            val year = DatePicker.year
            calendar.set(Calendar.DAY_OF_MONTH, dayofmonth)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.YEAR, year)


            showtimePickerDialog(selecttime)

                datePickerDialog.dismiss()
        }


        datePickerDialog.show()
//        flag = 1

    }

    private fun onButtonClicked(button: AppCompatButton) {

        if(button == lastpressedButton)
        {
            resetButtonColor(button)
            lastpressedButton = null
        }
        else{
            resetLastPressedButton()
            setButtonColor(button)
            lastpressedButton = button
        }
    }

    private fun resetLastPressedButton() {

        lastpressedButton?.let { resetButtonColor(it) }
    }

    private fun setButtonColor(button:AppCompatButton)
    {

        button.backgroundTintList = buttonColorsMap[button]
        todocategory = button.text.toString()
    }

    private fun resetButtonColor(button: AppCompatButton)
    {

        val pressedColor = ColorStateList.valueOf(resources.getColor(R.color.backcolorlight))
        button.backgroundTintList = pressedColor
        todocategory = "General"

    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, EEEE", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }



    private fun sidebardisplay() {


        val dialogView = layoutInflater.inflate(R.layout.sidebar, null)

        val popupWindow = PopupWindow(
            dialogView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        popupWindow.animationStyle = R.style.PopupsideAnimation
        popupWindow.isFocusable = true

//        findViewById<CardView>(R.id.addcardView).visibility = View.GONE


        popupWindow.setOnDismissListener {
//            findViewById<CardView>(R.id.addcardView).visibility = View.VISIBLE

        }


        popupWindow.showAtLocation(dialogView, Gravity.LEFT, 0, 0)

    }



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toDoAdapter = ToDoAdapter(this)

        binding.rvToDoItems.adapter = toDoAdapter
        binding.rvToDoItems.layoutManager = LinearLayoutManager(this)


        todocount = toDoAdapter.itemCount


        updateEmptyStateVisibility()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SET_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SET_ALARM),
                1
            )
        }


        binding.tvdatetoday.text = getCurrentDate()
        binding.tvnooftask.text = "${todocount} Task Pending"



        binding.add.setOnClickListener {

                addtaskfunc()
                timeflag = 0

            }

        binding.btndelete.setOnClickListener {
                toDoAdapter.deletedonetodos()
                todocount = toDoAdapter.itemCount
                binding.tvnooftask.text = "${todocount} Task Pending"
                updateEmptyStateVisibility()

            }


        binding.imgbtndeck.setOnClickListener {
                sidebardisplay()
            }

    }

}


