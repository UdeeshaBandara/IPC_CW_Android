package com.ipc.cw.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Car (
    @DocumentId
    var uID: String? = null,
    var carNo: String? = null,
    var driverName: String? = null,
    var locationList: List<Location> = emptyList()
)
data class Location (
    @DocumentId
    var uID: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var timestampe: Int? = null
)