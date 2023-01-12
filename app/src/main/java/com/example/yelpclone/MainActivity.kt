package com.example.yelpclone

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.yelpclone.ui.theme.YelpCloneTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.example.yelpclone.ui.theme.Shapes
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarConfig

//THIS ONE

private const val BASE_URL = "https://api.yelp.com/v3/"
private const val TAG = "MainActivity!"
private const val API_KEY = "YmVdWwEeSWkl4i-IhYslK8_q_iwwNfMTHq0aHrB9Js44sDYUhZuVD1AYcLk8DKNdJaqxERX_6GcWarIbizfR2T_XcAit_0jdPK1JZEyyy28alxC-2pUndEwj_TO8Y3Yx"
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isOnline(this)){
            mainScreen()
        }
        else{
            setContent {
                YelpCloneTheme {
                    DefaultAppBar()
                    ScreenForNoInternet(context = this)
                }
            }
            Toast.makeText(this,"Please connect to internet for the app to work!", Toast.LENGTH_LONG).show()
        }
    }

    private fun mainScreen(){
        val restaurants = mutableListOf<YelpRestaurant>()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY", "Sushi", "New York")
            .enqueue(object : Callback<YelpSearchResult> {
                override fun onResponse(
                    call: Call<YelpSearchResult>,
                    response: Response<YelpSearchResult>
                ) {
                    Log.i(TAG, "onResponse ${response.body()?.restaurants}")
                    val body = response.body()
                    if (body != null) {
                        restaurants.addAll(body.restaurants)
                        Log.i(TAG, "onAdd $restaurants")
                        setContent {
                            YelpCloneTheme {
                                DisplayList(restaurants)
                            }
                        }
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                }
            })
        Toast.makeText(this,"Showing Results For Sushi in New York", Toast.LENGTH_LONG).show()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    @Composable
    fun ScreenForNoInternet(context: Context){
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally){
            Button(onClick = {
                if(isOnline(context)){
                    mainScreen()
                }
            }) {
                Text("Refresh")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }

    @Composable
    fun DisplayList(restaurants: MutableList<YelpRestaurant>) {
        Scaffold(
            topBar = {
                DefaultAppBar()
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    LazyColumn() {
                        items(restaurants) { restaurant ->
                            ShowRestaurants(
                                restaurant.imageURL,
                                restaurant.name,
                                restaurant.rating.toFloat(),
                                restaurant.numReviews,
                                restaurant.location,
                                restaurant.categories,
                                restaurant.distanceInKMS()
                            )
                        }
                    }
                }

            }
        )
    }

    @Composable
    fun DefaultAppBar() {
        TopAppBar(title = { Text(text = "Yelp Clone") },
            actions = {}
        )
    }

    @Composable
    fun ShowRestaurants(
        imageURL: String,
        nameOfShop: String,
        rating: Float,
        numReviews: Int,
        location: YelpLocation,
        categories: List<YelpCategories>,
        distance: String
    ) {
        Card(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            shape = RectangleShape,
            elevation = 7.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imageURL,
                    modifier = Modifier
                        .sizeIn(
                            maxHeight = 100.dp,
                            maxWidth = 100.dp,
                            minHeight = 100.dp,
                            minWidth = 100.dp
                        )
                        .padding(5.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentDescription = null,
                    alignment = Alignment.Center, contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(5.dp))
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = nameOfShop,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 3.dp),
                        color = Color.DarkGray
                    )
                    Row {
                        RatingBar(
                            value = rating,
                            onValueChange = {},
                            onRatingChanged = {},
                            config = RatingBarConfig().size(11.dp).activeColor(
                                Color.Magenta
                            ).inactiveColor(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$numReviews Reviews",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Start,
                            color = Color.Gray
                        )
                    }
                    Text(text = location.address1, fontSize = 11.sp)
                    var categoriesToDisplay = categories[0].title
                    for (category in categories) {
                        if (category.title != categoriesToDisplay) {
                            categoriesToDisplay = categoriesToDisplay + ", " + category.title
                        }
                    }
                    Text(text = categoriesToDisplay, fontSize = 10.sp, color = Color.Gray)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, end = 6.dp), horizontalAlignment = Alignment.End
                ) {
                    Text(text = distance, fontSize = 9.sp)
                    Text(text = "$$", fontSize = 9.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}