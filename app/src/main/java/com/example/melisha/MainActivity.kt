package com.example.melisha

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.melisha.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList
import com.example.melisha.helpers.Message
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var sessionsClient:SessionsClient?=null
    private var sessionName:SessionName?=null
    private val uuid = UUID.randomUUID().toString()
    private lateinit var chatAdapter: ChatAdapter
    private var messagelist:ArrayList<Message> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this).load(R.drawable.ai).into(binding.imageview)

        setSupportActionBar(binding.toolbar)
        chatAdapter= ChatAdapter(this,messagelist)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=chatAdapter
        binding.fabsend.setOnClickListener {
            val message:String=binding.editTextmessages.text.toString()
             if(binding.editTextmessages.text.isNotEmpty())
             {
                addmessagetolist(message,false)
                 sendmessagetoBot(message)
             }
             else
             {
                 Toast.makeText(this, "Please enter text!", Toast.LENGTH_SHORT).show()
             }
         }
        setupBot()
    }

    private fun setupBot() {
        try {

            val stream = this.resources.openRawResource(R.raw.credential)
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
            val projectId: String = (credentials as ServiceAccountCredentials).projectId
            val settingsBuilder: SessionsSettings.Builder = SessionsSettings.newBuilder()
            val sessionsSettings: SessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)
            ).build()
            sessionsClient = SessionsClient.create(sessionsSettings)
            sessionName = SessionName.of(projectId, uuid)
        }catch (e: Exception) {
            Log.d(TAG, "setUpBot: " + e.message)
        }
    }

    private fun sendmessagetoBot(message: String) {
        val input=QueryInput.newBuilder().setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build()
        GlobalScope.launch {
            sendMessageinBg(input)
        }

    }
    private suspend fun sendMessageinBg(queryInput: QueryInput)
    {
        withContext(Dispatchers.Default){
            try {
                val detectIntentRequest=DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .build()
                val result=sessionsClient?.detectIntent(detectIntentRequest)
                if(result!=null){
                    runOnUiThread {
                        updateUI(result)
                    }
                }
                else{
                    Toast.makeText(this@MainActivity, "something went wrong", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                Log.d(TAG, "messgaeinbg: " + e.message)
            }
        }
    }

    private fun updateUI(result: DetectIntentResponse) {
        val fullfillmenttextmessages=result.queryResult.fulfillmentMessagesList
        val fullfillmentallmessages=result.queryResult.fulfillmentMessagesCount-1
        var sumbotreply:String=fullfillmenttextmessages[0].text.getText(0).toString()
        if(fullfillmentallmessages>0)
        sumbotreply+="\n"
        for(i in 1..fullfillmentallmessages){
            if(i==fullfillmentallmessages)
            sumbotreply+=fullfillmenttextmessages[i].text.getText(0).toString()
            else
            sumbotreply+=fullfillmenttextmessages[i].text.getText(0).toString()+"\n"
        }

        if(sumbotreply.isNotEmpty())
        {
            addmessagetolist(sumbotreply,true)
        }
        else{
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addmessagetolist(message: String, isrecieved: Boolean) {
        messagelist.add(Message(message,isrecieved))
        binding.editTextmessages.setText("")
        chatAdapter.notifyDataSetChanged()
        binding.recyclerView.layoutManager?.scrollToPosition(messagelist.size-1)
    }
}