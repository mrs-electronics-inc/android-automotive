package com.mrselectronics.gaugecluster

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gearValueText: TextView
    private lateinit var fuelGauge: GaugeView
    private lateinit var speedGauge: GaugeView
    private lateinit var rangeGauge: GaugeView
    private lateinit var rpmGauge: GaugeView

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private val propertyCallback =
        object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                runOnUiThread {
                    updateValue(carPropertyValue.propertyId, carPropertyValue)
                }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.e(TAG, "Error for property: $propId zone: $zone")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        configureGauges()
    }

    override fun onStart() {
        super.onStart()
        connectCar()
        subscribeToProperties()
    }

    override fun onStop() {
        unsubscribeFromProperties()
        disconnectCar()
        super.onStop()
    }

    private fun bindViews() {
        gearValueText = requireViewById(R.id.gear_value)
        fuelGauge = requireViewById(R.id.fuel_gauge)
        speedGauge = requireViewById(R.id.speed_gauge)
        rangeGauge = requireViewById(R.id.range_gauge)
        rpmGauge = requireViewById(R.id.rpm_gauge)
    }

    private fun connectCar() {
        if (carPropertyManager != null) {
            return
        }

        val connectedCar = Car.createCar(this)
        if (connectedCar == null) {
            Log.e(TAG, "Unable to create Car instance")
            return
        }

        val manager = connectedCar.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
        if (manager == null) {
            Log.e(TAG, "Car property manager unavailable")
            connectedCar.disconnect()
            return
        }

        car = connectedCar
        carPropertyManager = manager
    }

    private fun disconnectCar() {
        carPropertyManager = null
        car?.disconnect()
        car = null
    }

    private fun subscribeToProperties() {
        val manager = carPropertyManager ?: return

        subscribe(manager, VehiclePropertyIds.GEAR_SELECTION)
        subscribe(manager, VehiclePropertyIds.FUEL_LEVEL)
        subscribe(manager, VehiclePropertyIds.PERF_VEHICLE_SPEED)
        subscribe(manager, VehiclePropertyIds.RANGE_REMAINING)
        subscribe(manager, VehiclePropertyIds.ENGINE_RPM)
    }

    private fun unsubscribeFromProperties() {
        carPropertyManager?.unregisterCallback(propertyCallback)
    }

    private fun subscribe(manager: CarPropertyManager, propId: Int) {
        try {
            manager.registerCallback(
                propertyCallback,
                propId,
                CarPropertyManager.SENSOR_RATE_ONCHANGE
            )
        } catch (securityException: SecurityException) {
            Log.e(TAG, "No permission for property: $propId", securityException)
        }
    }

    private fun updateValue(propId: Int, value: CarPropertyValue<*>) {
        if (value.status != CarPropertyValue.STATUS_AVAILABLE) {
            return
        }

        val rawValue = value.value as? Number ?: return

        when (propId) {
            VehiclePropertyIds.GEAR_SELECTION -> gearValueText.text = formatGear(rawValue.toInt())
            VehiclePropertyIds.FUEL_LEVEL -> {
                val fuelCapacity = getFuelCapacity()
                if (fuelCapacity > 0f) {
                    fuelGauge.setValue(rawValue.toFloat() / fuelCapacity * 100f)
                }
            }
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> speedGauge.setValue(convertSpeed(rawValue.toFloat()))
            VehiclePropertyIds.RANGE_REMAINING -> rangeGauge.setValue(convertRange(rawValue.toFloat()))
            VehiclePropertyIds.ENGINE_RPM -> rpmGauge.setValue(rawValue.toFloat())
        }
    }

    private fun configureGauges() {
        val imperial = isImperialLocale()
        speedGauge.configure(
            getString(R.string.label_speed),
            if (imperial) "mph" else "km/h",
            0f,
            if (imperial) MAX_SPEED_MPH else MAX_SPEED_KMH
        )
        rpmGauge.configure(getString(R.string.label_rpm), "rpm", 0f, MAX_RPM)
        fuelGauge.configure(getString(R.string.label_fuel), "%", 0f, 100f)
        rangeGauge.configure(
            getString(R.string.label_range),
            if (imperial) "mi" else "km",
            0f,
            if (imperial) MAX_RANGE_MI else MAX_RANGE_KM
        )
    }

    private fun isImperialLocale(): Boolean {
        val country = resources.configuration.locales[0].country
        return country == "US" || country == "LR" || country == "MM"
    }

    private fun convertSpeed(metersPerSecond: Float): Float {
        return if (isImperialLocale()) {
            metersPerSecond * METERS_PER_SECOND_TO_MPH
        } else {
            metersPerSecond * METERS_PER_SECOND_TO_KMH
        }
    }

    private fun convertRange(meters: Float): Float {
        return if (isImperialLocale()) {
            meters / METERS_PER_MILE
        } else {
            meters / METERS_PER_KILOMETER
        }
    }

    private fun getFuelCapacity(): Float {
        val manager = carPropertyManager ?: return 0f
        val fuelCapacityValue = manager.getProperty(VehiclePropertyIds.INFO_FUEL_CAPACITY, 0)
            ?: return 0f
        if (fuelCapacityValue.status != CarPropertyValue.STATUS_AVAILABLE) {
            return 0f
        }

        return (fuelCapacityValue.value as? Number)?.toFloat() ?: 0f
    }

    private fun formatGear(gearValue: Int): String {
        return when (gearValue) {
            1 -> "N"
            2 -> "R"
            4 -> "P"
            8 -> "D"
            else -> "?"
        }
    }

    companion object {
        private const val TAG = "GaugeCluster"
        private const val METERS_PER_SECOND_TO_MPH = 2.2369363f
        private const val METERS_PER_SECOND_TO_KMH = 3.6f
        private const val METERS_PER_MILE = 1609.344f
        private const val METERS_PER_KILOMETER = 1000f
        private const val MAX_SPEED_MPH = 120f
        private const val MAX_SPEED_KMH = 200f
        private const val MAX_RANGE_MI = 300f
        private const val MAX_RANGE_KM = 500f
        private const val MAX_RPM = 7000f
    }
}
