package com.business.lawco.fragment.filterDialog

import com.business.lawco.model.consumer.Data

interface FilterApply {
    fun apply(address:List<Data>,practice:List<String>, practiceId:MutableList<String>)
}