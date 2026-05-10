package com.smartcall.guard.data.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.smartcall.guard.data.entity.SettingsEntity
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
    private val settingsRepository: SettingsRepository
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var cachedCity: String? = null
    private var cachedProvince: String? = null
    private var cachedLatitude: Double? = null
    private var cachedLongitude: Double? = null
    private var cacheTimestamp: Long = 0L
    private var cachedLocation: Location? = null

    suspend fun getCurrentCity(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        if (cachedCity != null && System.currentTimeMillis() - cacheTimestamp < cacheExpiryMs) {
            return cachedCity
        }

        val location = getLastLocation() ?: requestNewLocation()
        if (location != null) {
            val city = getCityFromLocation(location)
            if (city != null) {
                cachedCity = LocationHelper.cleanCityName(city)
                cachedLatitude = location.latitude
                cachedLongitude = location.longitude
                cacheTimestamp = System.currentTimeMillis()
                return cachedCity
            }
        }

        return cachedCity
    }

    suspend fun getCurrentProvince(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        if (cachedProvince != null && System.currentTimeMillis() - cacheTimestamp < cacheExpiryMs) {
            return cachedProvince
        }

        val location = getLastLocation() ?: requestNewLocation()
        if (location != null) {
            val province = getProvinceFromLocation(location)
            if (province != null) {
                cachedProvince = province.trim().removeSuffix("省")
                cacheTimestamp = System.currentTimeMillis()
                return cachedProvince
            }
        }

        return cachedProvince
    }

    fun getCurrentCitySync(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        if (cachedCity != null && System.currentTimeMillis() - cacheTimestamp < cacheExpiryMs) {
            return cachedCity
        }
        return null
    }

    fun getCurrentProvinceSync(): String? {
        val settings = settingsRepository.getSettingsSync()
        val cacheMinutes = settings?.cacheLocationMinutes ?: 30
        val cacheExpiryMs = cacheMinutes * 60 * 1000L

        if (cachedProvince != null && System.currentTimeMillis() - cacheTimestamp < cacheExpiryMs) {
            return cachedProvince
        }
        return null
    }

    fun getCachedLocation(): CachedLocation? {
        if (cachedLatitude == null || cachedLongitude == null) return null
        return CachedLocation(cachedLatitude!!, cachedLongitude!!)
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
