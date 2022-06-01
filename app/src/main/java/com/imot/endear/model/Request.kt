package com.imot.endear.model

class Request {
    lateinit var to:String
    lateinit var data:Map<String,String>


    fun request(to:String, data:Map<String,String>){
        this.to = to
        this.data= data
    }


}