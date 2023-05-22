package dev.shreyaspatil.permissionFlow.example.ui.fragment

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import dev.shreyaspatil.permissionFlow.example.R

class ExampleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_example)
        findViewById<FrameLayout>(R.id.frameLayout).let { frameLayout ->
            supportFragmentManager
                .beginTransaction()
                .replace(frameLayout.id, ExampleFragment())
                .commit()
        }
    }
}