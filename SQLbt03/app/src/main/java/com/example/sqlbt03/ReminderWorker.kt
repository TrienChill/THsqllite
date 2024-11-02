package com.example.sqlbt03

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {


    override fun doWork(): Result {
        val noteId = inputData.getLong("note_id", -1)
        val noteTitle = inputData.getString("note_title") ?: return Result.failure()

        createNotificationChannel()

        // Sử dụng try-catch để kiểm tra quyền
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                throw SecurityException("POST_NOTIFICATIONS permission not granted")
            }

            showNotification(noteId, noteTitle)
            return Result.success()

        } catch (e: SecurityException) {
            // Log lỗi hoặc xử lý khi không có quyền
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for note reminders"
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(noteId: Long, noteTitle: String) {
        val intent = Intent(applicationContext, NoteDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("note_id", noteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            noteId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Note Reminder")
            .setContentText(noteTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(noteId.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "note_reminders"
    }
}
