package com.damc.smart_home

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.damc.smart_home.retrofit.ServiceLocator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    companion object {
        private const val ASR_PERMISSION_REQUEST_CODE = 0
    }

    lateinit var mUserUtteranceOutput: TextView

    lateinit var mUserInfoText: TextView

    lateinit var button: ImageView

    lateinit var lightButton: Button

    private var mSpeechRecognizer: SpeechRecognizer? = null
    private var mIsListening = false
    private var isLightOn = false
    private var mCommandsList: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mUserUtteranceOutput = findViewById(R.id.user_utterance_output)
        mUserInfoText = findViewById(R.id.user_info_text)
        button = findViewById(R.id.trigger_icon)
        lightButton = findViewById(R.id.bt_light_on)


        initCommands()

        verifyAudioPermissions()

        createSpeechRecognizer()

        onClickMic()

        lightOnButton()
    }

    private fun initCommands() {
        mCommandsList = ArrayList()
        mCommandsList!!.add("cart")
        mCommandsList!!.add("cancel")
        mCommandsList!!.add("home")
        mCommandsList!!.add("turn on")
        mCommandsList!!.add("turn off")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ASR_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // audio permission granted
                Toast.makeText(this, "You can now use voice commands!", Toast.LENGTH_LONG).show()
                createSpeechRecognizer()
            } else {
                // audio permission denied
                Toast.makeText(
                    this,
                    "Please provide microphone permission to use voice.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                print("onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                print("onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                print("onRmsChanged()")
            }

            override fun onBufferReceived(buffer: ByteArray) {
                print("onBufferReceived")
            }

            override fun onEndOfSpeech() {
                print("onEndOfSpeech")
                handleSpeechEnd()
            }

            override fun onError(error: Int) {
                print("onError")
                handleSpeechEnd()
            }

            override fun onResults(results: Bundle) {
                print("onResults")
                // Called when recognition results are ready. This callback will be called when the
                // audio session has been completed and user utterance has been parsed.
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // The results are added in decreasing order of confidence to the list
                    for(s in matches){
                        mUserUtteranceOutput!!.text = s
//                        handleCommand(s)
                        if (s.equals("light turn on")) {
                            lightOn()
                            break
                        } else if (s.equals("light turn off")) {
                            lightOff()
                            break
                        }
                    }

                }
            }

            override fun onPartialResults(partialResults: Bundle) {
                print("onPartialResults")
                // Called when partial recognition results are available, this callback will be
                // called each time a partial text result is ready while the user is speaking.
                val matches =
                    partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.size > 0) {
                    // handle partial speech results
                    val partialText = matches[0]

                    for (s in matches) {
                        mUserUtteranceOutput!!.text = s
                        if (s.equals("light turn on")) {
                            lightOn()
                            break
                        } else if (s.equals("light turn off")) {
                            lightOff()
                            break
                        }
//                        handleCommand(s)
                    }

                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                print("onEvent")
            }
        })
    }

    private fun verifyAudioPermissions() {
        if (checkCallingOrSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                ASR_PERMISSION_REQUEST_CODE
            )
        } else {
            createSpeechRecognizer()
        }
    }

    private fun handleCommand(command: String) {
        // Function to handle user commands - TBD
        if (mCommandsList!!.contains(command)) {
            // Successful utterance, notify user
            Toast.makeText(this, "Executing: $command", Toast.LENGTH_LONG).show()
            if (command.equals("turn on")) {
                lightOn()
            } else if (command.equals("turn off")) {
                lightOff()
            }
        } else {
            // Unsucessful utterance, show failure message on screen
            Toast.makeText(this, "Could not recognize command", Toast.LENGTH_LONG).show()
        }
    }

    private fun createIntent(): Intent {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        return i
    }

    private fun handleSpeechBegin() {
        // start audio session
        mUserInfoText!!.setText("Listening....")
        mIsListening = true
        mSpeechRecognizer!!.startListening(createIntent())
    }

    private fun handleSpeechEnd() {
        // end audio session
        mUserInfoText!!.setText("Speech detected")
        mIsListening = false
        mSpeechRecognizer!!.cancel()
    }

    fun onClickMic() {
        button.setOnClickListener {
            if (mIsListening) {
                handleSpeechEnd()
            } else {
                handleSpeechBegin()
            }
        }
    }

    fun lightOn() {
        try {
            ServiceLocator().getInstance()?.getApi()?.ligtOn()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    { response -> onGetReqSampleResponse(response) },
                    { t -> GetReqSampleFailure(t) }
                )
        } catch (e: Exception) {
            //throw RuntimeException(e)
            e.printStackTrace()
        }
    }

    fun lightOff() {
        try {
            ServiceLocator().getInstance()?.getApi()?.ligtOff()
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeOn(Schedulers.io())
                ?.subscribe(
                    { response -> onGetReqSampleResponse(response) },
                    { t -> GetReqSampleFailure(t) }
                )
        } catch (e: Exception) {
            //throw RuntimeException(e)
            e.printStackTrace()
        }
    }

    private fun onGetReqSampleResponse(response: Response<ResponseBody>?) {
        if (response?.code() == 200) {

            Toast.makeText(this, "light......", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                response?.code().toString() + " " + response?.message(),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun GetReqSampleFailure(t: Throwable) {
        Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
    }

    fun lightOnButton() {
        lightButton.setOnClickListener {
            if (isLightOn) {
                isLightOn = !isLightOn
                lightOff()
            } else {
                isLightOn = !isLightOn
                lightOn()
            }
        }
    }
}