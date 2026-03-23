package com.business.lawco.model.consumer

data class SelectAddressModel(
    val `data`: List<Data>,
    val message: String,
    val status: Boolean
)