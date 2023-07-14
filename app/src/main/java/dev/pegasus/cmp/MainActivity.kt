package dev.pegasus.cmp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.pegasus.cmp.managers.ConsentManager

class MainActivity : AppCompatActivity() {

    private val consentManager by lazy { ConsentManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TAG", "onCreate: called")
        when (BuildConfig.DEBUG) {
            true -> consentManager.initDebugConsent("DDB1E6F0E65290BE5D2AA454D6414C60", callback)
            false -> consentManager.initConsent(callback)
        }
    }

    private val callback: ((errorMessage: String?) -> Unit) = { errorMessage ->
        Log.d("TAG", "callback: $errorMessage")
        if (errorMessage != null) {
            showToast(errorMessage)
        }
    }

    private fun showToast(message: Any) {
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
    }
}