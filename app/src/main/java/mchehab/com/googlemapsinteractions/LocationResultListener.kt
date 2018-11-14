package mchehab.com.googlemapsinteractions

import android.location.Location

interface LocationResultListener {
    fun getLocation(location: Location)
}