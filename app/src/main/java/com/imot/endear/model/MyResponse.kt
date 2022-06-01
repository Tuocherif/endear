package com.imot.endear.model

class MyResponse {
    var multicast_id :Long = 0.toLong()
    var success : Int = 0
    var failure : Int = 0
    var canonical_ids : Int = 0
    var results: List<Result>? = null


}