package com.example.displayapp

import android.util.Log

object VendorVehiclePropertyIds {
    private const val TAG = "display-app"
    private const val CLASS_NAME = "vendor.nlab.vehicle.V1_0.VehicleProperty"

    val vendorTest1sCounter: Int? = loadField("VENDOR_TEST_1S_COUNTER")
    val vendorTest500msCounter: Int? = loadField("VENDOR_TEST_500MS_COUNTER")
    val vendorTestSysProp: Int? = loadField("VENDOR_TEST_SYS_PROP")

    private fun loadField(fieldName: String): Int? {
        return try {
            val clazz = Class.forName(CLASS_NAME)
            clazz.getField(fieldName).getInt(null)
        } catch (e: ReflectiveOperationException) {
            Log.w(TAG, "Vendor vehicle property '$fieldName' is unavailable", e)
            null
        }
    }
}
