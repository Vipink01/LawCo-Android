package com.business.lawco.model.consumer

data class SelectedAttorneyModel(
    var ImageAttorney: Int,
    var TextAttorneyName: String,
    var TextTypeofAttorney: String,
    var TextLocation: String,
    var TextLocationAway: String,
    var connect: Boolean = false
)