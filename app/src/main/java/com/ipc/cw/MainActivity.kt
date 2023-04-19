package com.ipc.cw

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging

import com.ipc.cw.model.Car
import com.ipc.cw.model.Location


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    var locationPermissionGranted: Boolean = false
    private var map: GoogleMap? = null
    var mapView: View? = null
    private var mDatabase: DatabaseReference? = null
    var database: FirebaseFirestore? = null
    var authReference: CollectionReference? = null
    var carList  = mutableListOf<Car>()
    var locationList = mutableListOf<List<Location>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment?

        mapView = mapFragment?.view
        mapFragment?.getMapAsync(this)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        mDatabase = FirebaseDatabase.getInstance().reference
        database = FirebaseFirestore.getInstance()
        authReference = database!!.collection("carsList")

        authReference!!.addSnapshotListener(EventListener { carListSnapShot, e ->
            if (e != null) {
                return@EventListener
            }


            carList  = carListSnapShot!!.toObjects(Car::class.java)
            for (queryDocumentSnapshot in carList) {

                authReference!!.document(queryDocumentSnapshot.uID!!).collection("locationList").addSnapshotListener { snapshotNested, e ->

                    locationList.add(snapshotNested!!.toObjects(Location::class.java))
                    var ww  = snapshotNested!!.toObjects(Location::class.java)

                    map?.addPolyline(
                        PolylineOptions().add( LatLng(ww[0].location!!.latitude,ww[0].location!!.longitude), LatLng(ww[1].location!!.latitude,ww[1].location!!.longitude),  LatLng(ww[0].location!!.latitude,ww[0].location!!.longitude))
                            .width
                                (12f)
                            .color(Color.RED)
                            .geodesic(true)
                    )
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(6.971312953677447, 79.97690780715868), 13f))

                }

            }

        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

    }
}