package com.ipc.cw

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.firestore.*
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
    lateinit var recyclerCar: RecyclerView
    lateinit var carBottomSheet: CarBottomSheet

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

        authReference!!
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    carList = task.result.toObjects(Car::class.java)

                    Log.e("hiiiiii", carList[0].uID!!)

                }
            }

//        authReference!!.addSnapshotListener(EventListener { carListSnapShot, e ->
//            if (e != null) {
//                return@EventListener
//            }
//
//
//            carList  = carListSnapShot!!.toObjects(Car::class.java)
//            for (queryDocumentSnapshot in carList) {
//
//                authReference!!.document(queryDocumentSnapshot.uID!!).collection("locationList").addSnapshotListener { snapshotNested, e ->
//
//                    locationList.add(snapshotNested!!.toObjects(Location::class.java))
//                    var ww  = snapshotNested!!.toObjects(Location::class.java)
//
//                    map?.addPolyline(
//                        PolylineOptions().add( LatLng(ww[0].location!!.latitude,ww[0].location!!.longitude), LatLng(ww[1].location!!.latitude,ww[1].location!!.longitude),  LatLng(ww[0].location!!.latitude,ww[0].location!!.longitude))
//                            .width
//                                (12f)
//                            .color(Color.RED)
//                            .geodesic(true)
//                    )
//                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(6.971312953677447, 79.97690780715868), 13f))
//
//                }
//
//            }
//
//        })

        findViewById<TextView>(R.id.select_car).setOnClickListener {
            carBottomSheet = CarBottomSheet(this)
            carBottomSheet.setContentView(R.layout.bottom_sheet_car)
            carBottomSheet.show()
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

    }

    inner class CarBottomSheet(context: Context) :
        BottomSheetDialog(context) {


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            recyclerCar = this.findViewById(R.id.recycler_cars)!!


            this.findViewById<ImageView>(R.id.btn_clear)!!.setOnClickListener {
                carBottomSheet.dismiss()

            }

            recyclerCar.adapter = DistrictAdapter()
            recyclerCar.layoutManager = LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )


        }
    }

    private inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var car_name: TextView = itemView.findViewById(R.id.car_name)


    }

    private inner class DistrictAdapter : RecyclerView.Adapter<CarViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_car, parent, false)

            return CarViewHolder(view)
        }

        override fun getItemCount(): Int {
            return carList.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
            try {
                holder.car_name.text =
                    "${carList[position].carNo} ${carList[position].driverName}"


                holder.car_name.setOnClickListener {


                }
            } catch (e: Exception) {
                e.stackTrace
            }
        }
    }
}