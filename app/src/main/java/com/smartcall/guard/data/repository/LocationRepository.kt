package com.smartcall.guard.data.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartcall.guard.utils.LocationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @Volatile
    private var cache: LocationCache = LocationCache()

    private val cacheLock = Any()

    private data class LocationCache(
        val city: String? = null,
        val province: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val timestamp: Long = 0L
    )

    suspend fun getCurrentCity(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        synchronized(cacheLock) {
            if (cache.city != null && System.currentTimeMillis() - cache.timestamp < cacheExpiryMs) {
                return cache.city
            }
        }

        val location = getLastLocation() ?: requestNewLocation()
        if (location != null) {
            val city = getCityFromLocation(location)
            if (city != null) {
                val cleanedCity = LocationHelper.cleanCityName(city)
                synchronized(cacheLock) {
                    cache = cache.copy(
                        city = cleanedCity,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis()
                    )
                }
                return cleanedCity
            }
        }

        synchronized(cacheLock) {
            return cache.city
        }
    }

    suspend fun getCurrentProvince(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        synchronized(cacheLock) {
            if (cache.province != null && System.currentTimeMillis() - cache.timestamp < cacheExpiryMs) {
                return cache.province
            }
        }

        val location = getLastLocation() ?: requestNewLocation()
        if (location != null) {
            val province = getProvinceFromLocation(location)
            if (province != null) {
                val cleanedProvince = province.trim().removeSuffix("省")
                synchronized(cacheLock) {
                    cache = cache.copy(
                        province = cleanedProvince,
                        timestamp = System.currentTimeMillis()
                    )
                }
                return cleanedProvince
            }
        }

        synchronized(cacheLock) {
            return cache.province
        }
    }

    fun getCurrentCitySync(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        synchronized(cacheLock) {
            if (cache.city != null && System.currentTimeMillis() - cache.timestamp < cacheExpiryMs) {
                return cache.city
            }
        }
        return null
    }

    fun getCurrentProvinceSync(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        synchronized(cacheLock) {
            if (cache.province != null && System.currentTimeMillis() - cache.timestamp < cacheExpiryMs) {
                return cache.province
            }
        }
        return null
    }

    fun getCachedLocation(): CachedLocation? {
        synchronized(cacheLock) {
            if (cache.latitude == null || cache.longitude == null) return null
            return CachedLocation(cache.latitude!!, cache.longitude!!)
        }
    }

    private suspend fun getLastLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun requestNewLocation(): Location? {
        return try {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }.addOnFailureListener { _ ->
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }.addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCityFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.let { address ->
                address.locality ?: address.subAdminArea
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun getProvinceFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.adminArea
        } catch (e: IOException) {
            null
        }
    }

    data class CachedLocation(
        val latitude: Double,
        val longitude: Double
    )
}
