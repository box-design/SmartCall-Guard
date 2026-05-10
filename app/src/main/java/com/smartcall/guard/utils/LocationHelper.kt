package com.smartcall.guard.utils

object LocationHelper {
    private val municipalities = setOf("北京", "上海", "天津", "重庆")

    fun cleanCityName(city: String): String {
        return city.trim().removeSuffix("市")
    }

    fun isSameCity(city1: String?, city2: String?): Boolean {
        if (city1 == null || city2 == null) return false
        return cleanCityName(city1) == cleanCityName(city2)
    }

    fun isSameProvince(province1: String?, province2: String?): Boolean {
        if (province1 == null || province2 == null) return false
        return province1.trim().removeSuffix("省") == province2.trim().removeSuffix("省")
    }

    fun isMunicipality(city: String): Boolean {
        val cleaned = cleanCityName(city)
        return municipalities.contains(cleaned)
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
