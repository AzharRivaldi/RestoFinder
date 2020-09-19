package com.azhar.restofinder.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.azhar.restofinder.R
import com.azhar.restofinder.adapter.MainAdapter
import com.azhar.restofinder.adapter.MainAdapterHorizontal
import com.azhar.restofinder.model.ModelMain
import com.azhar.restofinder.model.ModelMainHorizontal
import com.azhar.restofinder.networking.ApiEndpoint
import com.azhar.restofinder.utils.OnItemClickCallback
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {

    private var mainAdapterHorizontal: MainAdapterHorizontal? = null
    private var mainAdapter: MainAdapter? = null
    private var mProgressBar: ProgressDialog? = null
    private val modelMainHorizontal: MutableList<ModelMainHorizontal> = ArrayList()
    private var modelMain: MutableList<ModelMain> = ArrayList()
    var lat: Double? = null
    var lng: Double? = null
    var permissionArrays = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = resources.getColor(R.color.colorPrimary)
        }

        val MyVersion = Build.VERSION.SDK_INT
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (checkIfAlreadyhavePermission() && checkIfAlreadyhavePermission2()) {
            } else {
                requestPermissions(permissionArrays, 101)
            }
        }

        mProgressBar = ProgressDialog(this)
        mProgressBar?.setTitle("Mohon Tunggu")
        mProgressBar?.setCancelable(false)
        mProgressBar?.setMessage("Sedang menampilkan data...")

        searchResto.setQueryHint("Cari Restoran")
        searchResto.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                setSearchResto(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") getListResto()
                return false
            }
        })

        val searchPlateId = searchResto.getContext()
                .resources.getIdentifier("android:id/search_plate", null, null)
        val searchPlate = searchResto.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        //method recyclerview
        showRecyclerResto()

        //method get lokasi
        getLatLong()
    }

    private fun showRecyclerResto() {
        mainAdapterHorizontal = MainAdapterHorizontal(this, modelMainHorizontal)
        mainAdapter = MainAdapter(this, modelMain)

        rvCollection.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        rvCollection.setHasFixedSize(true)
        rvCollection.setAdapter(mainAdapterHorizontal)

        rvRestaurantsNearby.setLayoutManager(LinearLayoutManager(this))
        rvRestaurantsNearby.setHasFixedSize(true)
        rvRestaurantsNearby.setAdapter(mainAdapter)

        mainAdapter?.setOnItemClickCallback(object : OnItemClickCallback {
            override fun onItemMainClicked(modelMain: ModelMain?) {
                val intent = Intent(this@MainActivity, DetailRestoActivity::class.java)
                intent.putExtra(DetailRestoActivity.DETAIL_RESTO, modelMain)
                startActivity(intent)
            }
        })
    }

    private fun getLatLong() {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 115)
            return
        }
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, true)
        val location = locationManager.getLastKnownLocation(provider)

        if (location != null) {
            onLocationChanged(location)

            //method get Collection
            getListCollection()

            //method get Daftar Resto
            getListResto()

        } else {
            locationManager.requestLocationUpdates(provider, 20000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        lng = location.longitude
        lat = location.latitude
    }

    private fun setSearchResto(query: String) {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.CariResto + query + "&lat=" + lat + "&lon=" + lng + "&radius=20000")
                .addHeaders("user-key", "ZOMATO API KEY")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            mProgressBar?.dismiss()
                            if (modelMain.isNotEmpty()) modelMain.clear()
                            val jsonArray = response.getJSONArray("restaurants")

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val dataApi = ModelMain()
                                val jsonObjectData = jsonObject.getJSONObject("restaurant")
                                val jsonObjectDataTwo = jsonObjectData.getJSONObject("user_rating")
                                val AggregateRating = jsonObjectDataTwo.getDouble("aggregate_rating")
                                val jsonObjectDataThree = jsonObjectData.getJSONObject("location")

                                dataApi.idResto = jsonObjectData.getString("id")
                                dataApi.nameResto = jsonObjectData.getString("name")
                                dataApi.thumbResto = jsonObjectData.getString("thumb")
                                dataApi.ratingText = jsonObjectDataTwo.getString("rating_text")
                                dataApi.addressResto = jsonObjectDataThree.getString("locality_verbose")
                                dataApi.aggregateRating = AggregateRating
                                modelMain.add(dataApi)
                            }
                            showRecyclerResto()
                            mainAdapter?.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        mProgressBar?.dismiss()
                        Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun getListCollection() {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.Collection + "lat=" + lat + "&lon=" + lng)
                .addHeaders("user-key", "ZOMATO API KEY")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            mProgressBar?.dismiss()
                            val jsonArray = response.getJSONArray("collections")

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val dataApi = ModelMainHorizontal()
                                val jsonObjectData = jsonObject.getJSONObject("collection")

                                dataApi.imageUrl = jsonObjectData.getString("image_url")
                                dataApi.urlResto = jsonObjectData.getString("url")
                                dataApi.title = jsonObjectData.getString("title")
                                dataApi.description = jsonObjectData.getString("description")
                                modelMainHorizontal.add(dataApi)
                            }
                            mainAdapterHorizontal?.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        mProgressBar?.dismiss()
                        Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun getListResto() {
        mProgressBar?.show()
        AndroidNetworking.get(ApiEndpoint.BASEURL + ApiEndpoint.Geocode + "lat=" + lat + "&lon=" + lng)
                .addHeaders("user-key", "ZOMATO API KEY")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        try {
                            mProgressBar?.dismiss()
                            modelMain = ArrayList()
                            val jsonArray = response.getJSONArray("nearby_restaurants")

                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val dataApi = ModelMain()
                                val jsonObjectData = jsonObject.getJSONObject("restaurant")
                                val jsonObjectDataTwo = jsonObjectData.getJSONObject("user_rating")
                                val AggregateRating = jsonObjectDataTwo.getDouble("aggregate_rating")
                                val jsonObjectDataThree = jsonObjectData.getJSONObject("location")

                                dataApi.idResto = jsonObjectData.getString("id")
                                dataApi.nameResto = jsonObjectData.getString("name")
                                dataApi.thumbResto = jsonObjectData.getString("thumb")
                                dataApi.ratingText = jsonObjectDataTwo.getString("rating_text")
                                dataApi.addressResto = jsonObjectDataThree.getString("locality_verbose")
                                dataApi.aggregateRating = AggregateRating
                                modelMain.add(dataApi)
                            }
                            showRecyclerResto()
                            mainAdapter?.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Gagal menampilkan data!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        mProgressBar?.dismiss()
                        Toast.makeText(this@MainActivity, "Tidak ada jaringan internet!", Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun checkIfAlreadyhavePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun checkIfAlreadyhavePermission2(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                val intent = intent
                finish()
                startActivity(intent)
            } else {
                getLatLong()
            }
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }
}
