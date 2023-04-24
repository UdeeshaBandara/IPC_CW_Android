package com.ipc.cw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color.parseColor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.ipc.cw.model.Car
import com.ipc.cw.model.Location


class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private var map: GoogleMap? = null
    var mapView: View? = null
    private var mDatabase: DatabaseReference? = null
    var database: FirebaseFirestore? = null
    var authReference: CollectionReference? = null
    var carList  = mutableListOf<Car>()
    var locationList = mutableListOf<LatLng>()
    var markerList = mutableListOf<Marker>()
    lateinit var recyclerCar: RecyclerView
    lateinit var carBottomSheet: CarBottomSheet
    lateinit var selectCar: TextView
    lateinit var view_all: TextView


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

        selectCar = findViewById(R.id.select_car)

        mDatabase = FirebaseDatabase.getInstance().reference
        database = FirebaseFirestore.getInstance()
        authReference = database!!.collection("carsList")

        getAllValues()

        selectCar.setOnClickListener {
            carBottomSheet = CarBottomSheet(this)
            carBottomSheet.setContentView(R.layout.bottom_sheet_car)
            carBottomSheet.show()
        }

    }

    private fun getAllValues() {
        authReference!!
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    carList = task.result.toObjects(Car::class.java)
                    map?.clear()
                    carList.forEach {
                        updateValueListener(it.uID!!, it.carNo!!)
                    }

                }
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
            view_all = this.findViewById(R.id.view_all)!!


            recyclerCar.adapter = DistrictAdapter()
            recyclerCar.layoutManager = LinearLayoutManager(
                applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )

            view_all.setOnClickListener {
                selectCar.text = "All Cars"
                carBottomSheet.dismiss()
                getAllValues()
            }
        }
    }

    private inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var car_name: TextView = itemView.findViewById(R.id.car_name)
        var driver_name: TextView = itemView.findViewById(R.id.driver_name)
        var card: CardView = itemView.findViewById(R.id.card)


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
                    "${carList[position].carNo}"
                holder.driver_name.text =
                    "${carList[position].driverName}"


                holder.card.setOnClickListener {
                    map?.clear()
                    selectCar.text = "${carList[position].carNo} ${carList[position].driverName}"
                    carBottomSheet.dismiss()
                    updateValueListener(carList[position].uID!!, carList[position].carNo!!)

                }
            } catch (e: Exception) {
                e.stackTrace
            }
        }
    }

    fun updateValueListener(documentId: String, carNo: String) {
        authReference!!.document(documentId).collection("locationList")
            .addSnapshotListener { snapshotNested, _ ->


                locationList = snapshotNested!!.toObjects(Location::class.java).map {

                    LatLng(it.latitude!!, it.longitude!!)

                } as MutableList<LatLng>

                if (locationList.size > 0) {
                    map?.addPolyline(
                        PolylineOptions().addAll(locationList)
                            .width
                                (12f)
                            .color(parseColor("#$carNo"))
                            .geodesic(true)
                    )

                    val m: Marker? = map?.addMarker(
                        MarkerOptions()
                            .position(locationList[locationList.size - 1])
                            .icon(
                                bitmapFromVector(
                                    applicationContext,
                                    R.drawable.icon_car
                                )
                            ).title(carNo)

                    )

                    if (m != null && markerList.indexOfFirst { it.title.toString() == carNo } == -1) {
                        markerList.add(m)
                    } else {
                        val position = markerList.indexOfFirst { it.title.toString() == carNo }
                        markerList[position].remove()
                        markerList.removeAt(position)
                        if (m != null) {
                            markerList.add(m)
                        }
                    }

                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            locationList[locationList.size - 1],
                            16.5f
                        )
                    )
                }
            }
    }
    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {

        val vectorDrawable = ContextCompat.getDrawable(
            context, vectorResId
        )

        vectorDrawable!!.setBounds(
            0, 0, vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}