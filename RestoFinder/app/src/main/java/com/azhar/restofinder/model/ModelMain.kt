package com.azhar.restofinder.model

import java.io.Serializable

/**
 * Created by Azhar Rivaldi on 16-09-2020
 */

class ModelMain : Serializable {
    var idResto: String? = null
    var nameResto: String? = null
    var thumbResto: String? = null
    var ratingText: String? = null
    var addressResto: String? = null
    var aggregateRating = 0.0
}