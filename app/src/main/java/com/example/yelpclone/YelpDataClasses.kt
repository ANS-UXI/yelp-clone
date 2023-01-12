package com.example.yelpclone

import com.google.gson.annotations.SerializedName

data class YelpSearchResult(
    val total: Int,
    @SerializedName("businesses") val restaurants: List<YelpRestaurant>
)

data class YelpRestaurant(val name: String,
                          val rating: Double,
                          val price: String,
                          @SerializedName("review_count") val numReviews: Int,
                          val distance: Double,
                          @SerializedName("image_url") val imageURL: String,
                          val categories: List<YelpCategories>,
                          val location: YelpLocation) {

    fun distanceInKMS(): String {
        val distanceKM = distance * 0.001
        val distanceInString = String.format("%.2f km",distanceKM)
        return distanceInString
    }
}

data class YelpCategories(val title: String)

data class YelpLocation(val address1: String)