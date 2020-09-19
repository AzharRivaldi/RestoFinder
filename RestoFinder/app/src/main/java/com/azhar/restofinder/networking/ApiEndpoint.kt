package com.azhar.restofinder.networking

/**
 * Created by Azhar Rivaldi on 15-09-2020
 */

object ApiEndpoint {
    @JvmField
    var BASEURL = "https://developers.zomato.com/api/v2.1/"
    @JvmField
    var Collection = "collections?"
    @JvmField
    var Geocode = "geocode?"
    @JvmField
    var DetailRestaurant = "restaurant?res_id="
    @JvmField
    var ReviewRestaurant = "reviews?res_id="
    @JvmField
    var CariResto = "search?q="
}