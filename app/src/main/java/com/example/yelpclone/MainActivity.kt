package com.example.yelpclone

import android.os.Bundle
import android.util.Log
import android.widget.RatingBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.yelpclone.ui.theme.Shapes
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarConfig

//THIS ONE

private const val BASE_URL = "https://api.yelp.com/v3/"
private const val TAG = "MainActivity!"
private const val API_KEY = "YmVdWwEeSWkl4i-IhYslK8_q_iwwNfMTHq0aHrB9Js44sDYUhZuVD1AYcLk8DKNdJaqxERX_6GcWarIbizfR2T_XcAit_0jdPK1JZEyyy28alxC-2pUndEwj_TO8Y3Yx"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restaurants = mutableListOf<YelpRestaurant>()
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY","Avocado Toast", "New York").enqueue(object: Callback<YelpSearchResult> {
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse ${response.body()?.restaurants}")
                val body = response.body()
                if (body != null) {
                    restaurants.addAll(body.restaurants)
                    Log.i(TAG,"onAdd $restaurants")
                    setContent {
                        YelpCloneTheme{
                            DisplayList(restaurants)
                        }
                    }
                }
                else{
                    return
                }
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })
    }
}

@Composable
fun DisplayList(restaurants: MutableList<YelpRestaurant>) {
    LazyColumn(){
        items(restaurants){ restaurant ->
            ShowRestaurants(restaurant.imageURL,
                restaurant.name,
                restaurant.rating,
                restaurant.numReviews,
                restaurant.location,
                restaurant.categories,
                restaurant.distanceInKMS())
        }
    }
}

@Composable
fun ShowRestaurants(imageURL: String,
                    nameOfShop: String,
                    rating:Double,
                    numReviews: Int,
                    location: YelpLocation,
                    categories: List<YelpCategories>,
                    distance: String){
     Card(modifier = Modifier
         .padding(12.dp)
         .fillMaxWidth(),
         shape = RectangleShape,
         elevation = 7.dp) {
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
             Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.Start){
                 Text(text = nameOfShop, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
                 Row{
                     RatingBar(value = rating.toFloat(), onValueChange = {}, onRatingChanged = {}, config = RatingBarConfig().size(11.dp).activeColor(
                         Color.Magenta).inactiveColor(Color.LightGray))
                     Spacer(modifier = Modifier.width(10.dp))
                     Text(text = "$numReviews Reviews", fontSize = 10.sp, textAlign = TextAlign.Start)
                 }
                 Text(text = location.address1, fontSize = 12.sp)
                 var categoriesToDisplay = categories[0].title
                 for (category in categories){
                     if(category.title != categoriesToDisplay){
                         categoriesToDisplay = categoriesToDisplay + ", " + category.title
                     }
                 }
                 Text(text = categoriesToDisplay, fontSize = 10.sp)
             }
             Column(modifier = Modifier
                 .fillMaxWidth()
                 .padding(top = 2.dp, end = 6.dp), horizontalAlignment = Alignment.End) {
                 Text(text = distance, fontSize = 12.sp)
                 Text(text = "$$", fontSize = 12.sp, textAlign = TextAlign.Center)
             }
         }
     }
}