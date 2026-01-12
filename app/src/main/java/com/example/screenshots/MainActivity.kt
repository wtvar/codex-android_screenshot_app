package com.example.screenshots

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.screenshots.databinding.ActivityMainBinding
import com.example.screenshots.media.ScreenshotRepository
import com.example.screenshots.model.DayGroup
import com.example.screenshots.ui.ScreenshotAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter = ScreenshotAdapter()
    private lateinit var repository: ScreenshotRepository

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            loadScreenshots()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ScreenshotRepository(this)
        binding.screenshotList.layoutManager = LinearLayoutManager(this)
        binding.screenshotList.adapter = adapter

        if (hasMediaPermission()) {
            loadScreenshots()
        } else {
            requestMediaPermissions()
        }
    }

    private fun loadScreenshots() {
        val screenshots = repository.queryScreenshots()
        val groups = screenshots.groupBy { item ->
            LocalDate.ofInstant(
                Instant.ofEpochMilli(item.takenAtMillis),
                ZoneId.systemDefault()
            )
        }
            .map { (date, items) ->
                DayGroup(
                    date = date,
                    items = items.sortedByDescending { it.takenAtMillis }
                )
            }
            .sortedByDescending { it.date }

        adapter.submitGroups(groups)
    }

    private fun hasMediaPermission(): Boolean {
        val permissions = requiredPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestMediaPermissions() {
        permissionLauncher.launch(requiredPermissions())
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
