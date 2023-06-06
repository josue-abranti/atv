package com.cosmos.atv.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cosmos.atv.R
import com.cosmos.atv.databinding.ActivityMainBinding
import com.cosmos.atv.interfaces.FrequencyContract
import com.cosmos.atv.presenter.FrequencyPresenter
import com.cosmos.atv.model.Frequency
import utils.Constants
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), FrequencyContract.View {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textViewFrequency: TextView
    private lateinit var textViewFrequencyLeft: TextView
    private lateinit var textViewFrequencyRight: TextView
    private lateinit var frequencyPresenter: FrequencyPresenter
    private var buttonState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textViewFrequency = findViewById(R.id.frequency)
        textViewFrequencyLeft = findViewById(R.id.frequencyLeft)
        textViewFrequencyRight = findViewById(R.id.frequencyRight)

        checkPermissions()

        this.frequencyPresenter = FrequencyPresenter(this, this)

        frequencyPresenter.removeFrequencyDatabase()

        frequencyPresenter.addFrequenciesFromXml()

        binding.fab.setOnClickListener {
            // Alterna o estado do botão ao ser clicado
            buttonState = !buttonState
            if (buttonState) {
                frequencyPresenter.onButtonClickOn()
            } else {
                frequencyPresenter.onButtonClickOff()
                this.updateFrequency(Frequency(), 0.0, 0, Constants.Position.CENTER)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPermissions() {
        val permissions: ArrayList<String> = arrayListOf()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET)
        }

        if(permissions.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), Constants.PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // A permissão foi concedida pelo usuário
                // Continue com o fluxo normal da aplicação
            } else {
                // A permissão foi negada pelo usuário
                // Trate o cenário em que a permissão é necessária para a funcionalidade da aplicação
            }
        }
    }

    override fun updateFrequency(frequency: Frequency, frequencyValue: Double, color: Int, position: Constants.Position) {
        this.runOnUiThread {
            val decimalFormat = DecimalFormat("00.00")
            textViewFrequency.text = decimalFormat.format(frequencyValue) + "Hz\n" +decimalFormat.format(frequency.frequencyPitch) + "Hz\n"+ frequency.pitch
            textViewFrequencyLeft.setTextColor(color)
            textViewFrequencyRight.setTextColor(color)
            when (position) {
                Constants.Position.RIGHT -> {
                    textViewFrequencyRight.visibility = View.VISIBLE
                    textViewFrequencyLeft.visibility = View.INVISIBLE
                }
                Constants.Position.LEFT -> {
                    textViewFrequencyRight.visibility = View.INVISIBLE
                    textViewFrequencyLeft.visibility = View.VISIBLE
                }
                Constants.Position.CENTER -> {
                    textViewFrequencyRight.visibility = View.INVISIBLE
                    textViewFrequencyRight.visibility = View.INVISIBLE
                }
            }
        }
    }


}