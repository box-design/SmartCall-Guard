package com.smartcall.guard.data.entity

data class Segment(
    val prefix: String,
    val province: String,
    val city: String,
    val operator: String?,
    val areaCode: String?
)
