package com.cosmos.atv.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cosmos.atv.R
import com.cosmos.atv.controller.RealmController
import com.cosmos.atv.databinding.ActivityMainBinding
import com.google.android.material.card.MaterialCardView
import controller.AudioController
import controller.FrequencyController
import model.Frequency
import utils.Constants

class MainActivity : AppCompatActivity(), AudioCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textViewFrequency: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        checkPermissions()

        val frequencyController = FrequencyController()
        var audioController = AudioController()

        frequencyController.removeFrequencyDatabase()
        frequencyController.addFrequenciesFromXml(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewFrequency = findViewById(R.id.frequency)

        audioController.registerCallback(this)

        binding.fab.setOnClickListener {
            audioController.startRecording(this)
        /*view ->

            Snackbar.make(view, "Acorde: " + frequencyController.getFrequency(1)?.chord + "\n" + "Frequencia: " + frequencyController.getFrequency(1)?.frequency, Snackbar.LENGTH_LONG)//"Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()
             */
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

    override fun onFrequencyUpdated(frequency: Double) {
        // Atualize o TextView ou realize outras operações na view
        this.runOnUiThread {
            textViewFrequency.text = frequency.toString()
        }
    }
}