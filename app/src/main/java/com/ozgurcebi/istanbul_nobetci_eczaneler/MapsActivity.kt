package com.ozgurcebi.istanbul_nobetci_eczaneler

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.SphericalUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.Exception
import kotlin.math.roundToInt
class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var PharmacyList = mutableListOf<Pharmacy>()
    private var LocalPharmacyList = mutableListOf<Pharmacy>()
    private var MarkerList : HashMap<String,Marker> = HashMap()
    var mapFragment : SupportMapFragment? = null
    private lateinit var viewModel: MainViewModel
    private var globalDistance = 5000
    private lateinit var globalCurrentLatLng : LatLng
    private var initialized = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val tb: Toolbar = toolbar as Toolbar
        setSupportActionBar(tb)

        viewModel =  ViewModelProviders.of(this).get(MainViewModel::class.java)

        val radioGroup = radio_group
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(initialized)
            {
                if(radio1.id == checkedId)
                {
                    globalDistance = 5000
                    loadMarkers(globalCurrentLatLng,globalDistance)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(globalCurrentLatLng, 12f))
                }
                else if(radio2.id == checkedId)
                {
                    globalDistance = 10000
                    loadMarkers(globalCurrentLatLng,globalDistance)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(globalCurrentLatLng, 11f))
                }
                else if(radio3.id == checkedId)
                {
                    globalDistance = 25000
                    loadMarkers(globalCurrentLatLng,globalDistance)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(globalCurrentLatLng, 9.7f))
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermission()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.isMyLocationEnabled = true
        map.getUiSettings().setZoomControlsEnabled(true)
        map.setInfoWindowAdapter(
            CustomInfoWindowAdapter(
                this
            )
        );
        map.setOnInfoWindowClickListener{
            val builder = AlertDialog.Builder(this@MapsActivity)

            // Set the alert dialog title
            builder.setTitle(it.title)

            // Display a message on alert dialog
            builder.setMessage("Seçilen adrese rota oluşturmak istiyor musunuz?")

            // Set a positive button and its click listener on alert dialog
            builder.setPositiveButton("Evet"){dialog, which ->
                // Do something when user press the positive button
                it.hideInfoWindow()
                val latitude = it.position.latitude
                val longitude = it.position.longitude
                val gmmIntentUri : Uri = Uri.parse("google.navigation:q=" + latitude + "," + longitude)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                try{
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        Toast.makeText(applicationContext,"Rota oluşturuyor",Toast.LENGTH_LONG).show()
                        startActivity(mapIntent)
                    }
                }catch (e: Exception){
                    Log.e("hata", "onClick: NullPointerException: Couldn't open map." + e.message )
                    Toast.makeText(applicationContext,"Harita açılamadı", Toast.LENGTH_SHORT).show()
                }

            }

            // Display a negative button on alert dialog
            builder.setNegativeButton("Hayır"){dialog,which ->
            }

            // Finally, make the alert dialog using builder
            val dialog: AlertDialog = builder.create()

            // Display the alert dialog on app interface
            dialog.show()
        }
        // 1


        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            /*
            if(!isOnline())
            {
            Toast.makeText(applicationContext,"İnternet bağlantısı bulunamadı", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
            finish()
            }

             */
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                globalCurrentLatLng = currentLatLng

                loadMarkers(currentLatLng,globalDistance)
                initialized = true
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
            else
            {
                Toast.makeText(applicationContext,"Devam etmek için navigasyonu açın", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                finish()
            }
        }


    }

    private fun setUpMap() {
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)

    }

    private fun loadMarkers(cur: LatLng, distance: Int)
    {
        viewModel.finished = {
            PharmacyList = it
            if(PharmacyList.isNotEmpty())
            {
                LocalPharmacyList.clear()
                MarkerList.clear()
                LocalPharmacyList.add(
                    Pharmacy(
                        "ECZANELERİ SIRALAMAK İÇİN DOKUNUN",
                        0.0,
                        0.0,
                        "0"
                    )
                )
                runOnUiThread {
                    for(p in PharmacyList.indices)
                    {
                        placeMarkerOnMap(PharmacyList[p],cur,distance)
                    }
                    LocalPharmacyList.sortBy { it.address.toInt() }
                    val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, LocalPharmacyList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            if(p2>0)
                            {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(LocalPharmacyList.get(p2).latitude,LocalPharmacyList.get(p2).longtitude), 12f))
                                MarkerList[LocalPharmacyList[p2].name]!!.showInfoWindow()
                            }

                        }
                    }
                }
            }
        }
        map.clear()
        if(PharmacyList.isNotEmpty())
        {
            LocalPharmacyList.clear()
            MarkerList.clear()
            LocalPharmacyList.add(
                Pharmacy(
                    "ECZANELERİ SIRALAMAK İÇİN DOKUNUN",
                    0.0,
                    0.0,
                    "0"
                )
            )
            for(p in PharmacyList.indices)
            {
                placeMarkerOnMap(PharmacyList[p],cur,distance)
            }
            LocalPharmacyList.sortBy { it.address.toInt() }
            val adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, LocalPharmacyList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    if(p2>0)
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(LocalPharmacyList.get(p2).latitude,LocalPharmacyList.get(p2).longtitude), 12f))
                        MarkerList[LocalPharmacyList[p2].name]!!.showInfoWindow()
                    }
                }
            }
        }

    }

    private fun placeMarkerOnMap(p: Pharmacy, currentLocation: LatLng, distance: Int) {

        val markerOptions = MarkerOptions().position(LatLng(p.latitude,p.longtitude)).title(p.name).snippet(p.address).icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        val localDistance = SphericalUtil.computeDistanceBetween(currentLocation, LatLng(p.latitude,p.longtitude))
        if (localDistance < distance) {
            markerOptions.visible(true)
            val m = map.addMarker(markerOptions)
            MarkerList.put("${p.name} (${localDistance.roundToInt()}m)",m)
            LocalPharmacyList.add(
                Pharmacy(
                    "${p.name} (${localDistance.roundToInt()}m)",
                    p.latitude,
                    p.longtitude,
                    (localDistance.toInt().toString())
                )
            )
        }
    }

    /*
    fun isOnline(): Boolean {
        try {
            val ipAddr = InetAddress.getByName("google.com")
            //You can replace it with your name
            return !ipAddr.equals("")

        } catch (e: Exception) {
            return false
        }

    }

     */

    private fun checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        else
            setUpMap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if(permissions.isNotEmpty())
            {
                for(x in 0..permissions.size)
                {
                    if(permissions.get(x) == android.Manifest.permission.ACCESS_FINE_LOCATION)
                    {
                        if(grantResults.get(x) == PackageManager.PERMISSION_GRANTED)
                        {
                            setUpMap()
                            break
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
