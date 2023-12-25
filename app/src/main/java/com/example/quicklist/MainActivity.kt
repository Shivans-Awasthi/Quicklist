package com.example.quicklist

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quicklist.databinding.ActivityMainBinding
import com.example.quicklist.databinding.AddTaskBinding
import com.example.quicklist.databinding.LayoutInfoBinding
import com.google.android.material.timepicker.MaterialTimePicker
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                addtask()
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }


    private fun addtask(){
        val todoTitle = binding.etToDoTitle.text.toString()
        if (todoTitle.isNotEmpty()){
            val todo = Todo(todoTitle)
            toDoAdapter.addTodo(todo)
            findViewById<EditText>(R.id.etToDoTitle).text.clear()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toDoAdapter = ToDoAdapter(this)

        binding.rvToDoItems.adapter = toDoAdapter
        binding.rvToDoItems.layoutManager = LinearLayoutManager(this)

        binding.btninfo.setOnClickListener {
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
        }

        binding.btnAddToDo.setOnClickListener{
            addtask()
        }
        binding.btnDeleteDoneToDo.setOnClickListener {
            toDoAdapter.deletedonetodos()
        }
    }

    override fun onPause() {
        super.onPause()
    }
}


class SecondActivity : AppCompatActivity() {

    private lateinit var binding: LayoutInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnback.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        binding.btngotonotify.setOnClickListener {
            val intent = Intent(this, NotifActivity::class.java)
            startActivity(intent)

        }

    }
}



class NotifActivity : AppCompatActivity()
{


    private lateinit var binding: AddTaskBinding
    private lateinit var picker: MaterialTimePicker
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Inside onCreate or an initialization method
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel_id",
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }



        binding.btnnotif.setOnClickListener {

            if (binding.etToDonotifMessage.text.isEmpty())
            {
                Toast.makeText(this,"Title cannot be empty",Toast.LENGTH_SHORT).show()

            }
            else showTimePickerDialog()

        }

      }


    private fun showTimePickerDialog() {

        val dialogView = layoutInflater.inflate(R.layout.custom_timepicker, null)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val btnSetTime = dialogView.findViewById<Button>(R.id.btnSetTime)

        val timePickerDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSetTime.setOnClickListener {
                    val hourOfDay = timePicker.hour
                    val minute = timePicker.minute

                    // Handle the selected time
                    // For example, you can schedule the notification at this time
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    // Schedule the notification with the selected time
                    scheduleNotification(calendar.timeInMillis)

                    timePickerDialog.dismiss()
                    Toast.makeText(this,"Notification Scheduled Successfuly",Toast.LENGTH_SHORT).show()
                }

        timePickerDialog.show()
    }


    private fun scheduleNotification(timeInMillis: Long) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("message", binding.etToDonotifMessage.text.toString())

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
             flags
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}








