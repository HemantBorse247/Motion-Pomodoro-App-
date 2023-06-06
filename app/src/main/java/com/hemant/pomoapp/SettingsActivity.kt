package com.hemant.pomoapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hemant.pomoapp.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivitySettingsBinding? = null
    private var changesSaved: Int = 0

    //    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = DataStoreManager(this@SettingsActivity)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        hideStatusBar()


        //setting edit texts

        lifecycleScope.launch {
            // focus length
            if (dataStore.getFocusLengthFromDataStore() == null) {
                binding?.tvFocusLength?.setText("25")
            } else {
                binding?.tvFocusLength?.setText(dataStore.getFocusLengthFromDataStore())
            }
            if (dataStore.getLongBreakLengthFromDataStore() == null) {
                binding?.tvLongBreakLength?.setText("15")
            } else {
                binding?.tvLongBreakLength?.setText(dataStore.getLongBreakLengthFromDataStore())
            }
            if (dataStore.getShortBreakLengthFromDataStore() == null) {
                binding?.tvShortBreakLength?.setText("5")
            } else {
                binding?.tvShortBreakLength?.setText(dataStore.getShortBreakLengthFromDataStore())
            }
            if (dataStore.getPomoInterval() == null) {
                binding?.tvPomoUntilLongBreak?.setText("3")
            } else {
                binding?.tvPomoUntilLongBreak?.setText(dataStore.getPomoInterval())
            }
            if (dataStore.getAutoResumeTimer() == null) {
                binding?.swAutoResumeTimer?.isChecked = false
            } else {
                binding?.swAutoResumeTimer?.isChecked = dataStore.getAutoResumeTimer()!!
            }
            if (dataStore.getSound() == null) {
                binding?.swSound?.isChecked = false
            } else {
                binding?.swSound?.isChecked = dataStore.getSound()!!
            }
        }


        //onclick-listeners for textview and increase decrease buttons

        binding?.tvFocusLength?.setOnClickListener(this)
        binding?.ibFocusLengthInc?.setOnClickListener(this)
        binding?.ibFocusLengthDec?.setOnClickListener(this)

        binding?.tvShortBreakLength?.setOnClickListener(this)
        binding?.ibShortBreakLengthInc?.setOnClickListener(this)
        binding?.ibShortBreakLengthDec?.setOnClickListener(this)

        binding?.tvLongBreakLength?.setOnClickListener(this)
        binding?.ibLongBreakLengthInc?.setOnClickListener(this)
        binding?.ibLongBreakLengthDec?.setOnClickListener(this)

        binding?.tvPomoUntilLongBreak?.setOnClickListener(this)
        binding?.ibPomoUntilLongBreakInc?.setOnClickListener(this)
        binding?.ibPomoUntilLongBreakDec?.setOnClickListener(this)

        binding?.swAutoResumeTimer?.setOnClickListener(this)

        binding?.swSound?.setOnClickListener(this)

        binding?.btnSaveChanges?.setOnClickListener(this)


        //close activity
        val btnCloseSettings = binding?.btnCloseSettings
        btnCloseSettings?.setOnClickListener {
            if (changesSaved == 1) setResult(RESULT_OK)
            else setResult(RESULT_CANCELED)
            finish()
        }


    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ibFocusLengthInc -> {
                var focusLengthValue = binding?.tvFocusLength?.text.toString().toInt()
                if (focusLengthValue >= 5) {
                    if (focusLengthValue >= 60) {
                        focusLengthValue = 60
                    } else {
                        focusLengthValue += 5
                    }
                } else {
                    focusLengthValue += 1
                }
                binding?.tvFocusLength?.setText("$focusLengthValue")
            }

            R.id.ibFocusLengthDec -> {
                var focusLengthValue = binding?.tvFocusLength?.text.toString().toInt()
                if (focusLengthValue <= 5) {
                    focusLengthValue -= 1
                } else {
                    focusLengthValue -= 5
                }

                binding?.tvFocusLength?.setText("${if (focusLengthValue > 0) focusLengthValue else 1}")
            }

            R.id.ibShortBreakLengthInc -> {
                var shortBreakLengthValue = binding?.tvShortBreakLength?.text.toString().toInt()

                if (shortBreakLengthValue >= 60) {
                    shortBreakLengthValue = 60
                } else {
                    shortBreakLengthValue += 1
                }

                binding?.tvShortBreakLength?.setText("$shortBreakLengthValue")
            }

            R.id.ibShortBreakLengthDec -> {
                var shortBreakLengthValue = binding?.tvShortBreakLength?.text.toString().toInt()
                if (shortBreakLengthValue > 1) {
                    shortBreakLengthValue -= 1
                } else {
                    shortBreakLengthValue = 1
                }
                binding?.tvShortBreakLength?.setText("$shortBreakLengthValue")
            }

            R.id.ibLongBreakLengthInc -> {
                var longBreakLengthValue = binding?.tvLongBreakLength?.text.toString().toInt()
                if (longBreakLengthValue >= 60) {
                    longBreakLengthValue = 60
                } else {
                    longBreakLengthValue += 1
                }
                binding?.tvLongBreakLength?.setText("$longBreakLengthValue")
            }

            R.id.ibLongBreakLengthDec -> {
                var longBreakLengthValue = binding?.tvLongBreakLength?.text.toString().toInt()
                if (longBreakLengthValue > 1) {
                    longBreakLengthValue -= 1
                } else {
                    longBreakLengthValue = 1
                }
                binding?.tvLongBreakLength?.setText("$longBreakLengthValue")
            }

            R.id.ibPomoUntilLongBreakInc -> {
                var pomoUntilLongBreakValue = binding?.tvPomoUntilLongBreak?.text.toString().toInt()

                if (pomoUntilLongBreakValue >= 10) {
                    pomoUntilLongBreakValue = 10
                } else {
                    pomoUntilLongBreakValue += 1
                }

                binding?.tvPomoUntilLongBreak?.setText("$pomoUntilLongBreakValue")
            }

            R.id.ibPomoUntilLongBreakDec -> {
                var pomoUntilLongBreakValue = binding?.tvPomoUntilLongBreak?.text.toString().toInt()

                if (pomoUntilLongBreakValue <= 1) {
                    pomoUntilLongBreakValue = 1
                } else {
                    pomoUntilLongBreakValue -= 1
                }

                binding?.tvPomoUntilLongBreak?.setText("$pomoUntilLongBreakValue")
            }

//            R.id.swAutoResumeTimer -> {
//                if (binding?.swAutoResumeTimer!!.isChecked) {
//
//                }
//            }

            R.id.btnSaveChanges -> {
                lifecycleScope.launch {
                    changesSaved = 1
                    saveChanges()
                    Toast.makeText(
                        this@SettingsActivity, "Changes have been saved", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    suspend fun saveChanges() {

        var focusLengthValue = binding?.tvFocusLength!!.text.toString()
        focusLengthValue = if (focusLengthValue.toInt() > 60) "60" else focusLengthValue

        var shortBreakLengthValue = binding?.tvShortBreakLength!!.text.toString()
        shortBreakLengthValue =
            if (shortBreakLengthValue.toInt() > 60) "60" else shortBreakLengthValue

        var longBreakLengthValue = binding?.tvLongBreakLength!!.text.toString()
        longBreakLengthValue = if (longBreakLengthValue.toInt() > 60) "60" else longBreakLengthValue

        var pomoUntilLongBreakValue = binding?.tvPomoUntilLongBreak!!.text.toString()
        pomoUntilLongBreakValue =
            if (pomoUntilLongBreakValue.toInt() > 10) "10" else pomoUntilLongBreakValue

        val autoResumeTimer = binding?.swAutoResumeTimer!!.isChecked
        val sound = binding?.swSound!!.isChecked

        val settings = Settings(
            focusLengthValue,
            shortBreakLengthValue,
            longBreakLengthValue,
            pomoUntilLongBreakValue,
            autoResumeTimer,
            sound
        )
        dataStore.saveToDataStore(settings)
    }


    fun hideStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        // For devices with API level 30 and above, use the following code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            // For devices with API level below 30, use the following code
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    override fun onBackPressed() {
        if (changesSaved == 1) setResult(RESULT_OK)
        else setResult(RESULT_CANCELED)
        finish()
    }
}