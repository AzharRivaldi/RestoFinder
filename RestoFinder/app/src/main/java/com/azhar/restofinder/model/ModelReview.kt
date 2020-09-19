package com.azhar.restofinder.model

import java.io.Serializable

/**
 * Created by Azhar Rivaldi on 17-09-2020
 */

class ModelReview : Serializable {
    var reviewText: String? = null
    var reviewTime: String? = null
    var nameUser: String? = null
    var profileImage: String? = null
    var ratingReview = 0.0
}