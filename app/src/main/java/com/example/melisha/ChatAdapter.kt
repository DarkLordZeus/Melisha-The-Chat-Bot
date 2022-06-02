package com.example.melisha

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.melisha.databinding.AdapterMessgaeBinding
import com.example.melisha.helpers.Message

class ChatAdapter(private var activity: Activity, private var messagelist:ArrayList<Message>):RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    inner class ViewHolder(val binding:AdapterMessgaeBinding)
        :RecyclerView.ViewHolder(binding.root){
            var messagereceive=binding.messageReceive
            var messagesent=binding.messageSent
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.ViewHolder {
        val binding = AdapterMessgaeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatAdapter.ViewHolder, position: Int) {
        val message=messagelist[position]
        if(message.isreceived)
        {
            holder.messagereceive.visibility = View.VISIBLE
            holder.messagesent.visibility = View.GONE
            holder.messagereceive.text=message.message

        }
        else
        {

            holder.messagesent.visibility = View.VISIBLE
            holder.messagereceive.visibility = View.GONE
            holder.messagesent.text = message.message
        }

    }

    override fun getItemCount(): Int {
        return messagelist.size
    }
}