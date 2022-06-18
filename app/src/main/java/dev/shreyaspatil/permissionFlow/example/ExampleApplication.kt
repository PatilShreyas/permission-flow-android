package dev.shreyaspatil.permissionFlow.example

import android.app.Application
import android.util.Log
import dev.shreyaspatil.permissionFlow.PermissionFlow
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PermissionFlow.init(this)
    }
}