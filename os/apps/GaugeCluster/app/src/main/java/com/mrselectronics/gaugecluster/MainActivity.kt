package com.mrselectronics.gaugecluster

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var speedGauge: GaugeView
    private lateinit var rpmReadout: StatusReadoutView

    // Tile in the rail that shows the vehicle's primary energy reserve. It
    // reports fuel level on ICE/hybrid vehicles, or battery level on EVs,
    // depending on which properties the HAL exposes.
    private lateinit var energyReadout: StatusReadoutView
    private lateinit var rangeReadout: StatusReadoutView

    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private enum class EnergySource { NONE, FUEL, BATTERY }
    private var energySource: EnergySource = EnergySource.NONE
    // Capacity used to normalize the active energy reading to a 0..100 %.
    // Fuel: liters/gallons (whatever INFO_FUEL_CAPACITY reports).
    // Battery: watt-hours (INFO_EV_BATTERY_CAPACITY).
    private var energyCapacityCached: Float = 0f

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
        configureStaticViews()
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
        speedGauge = requireViewById(R.id.speed_gauge)
        rpmReadout = requireViewById(R.id.rpm_readout)
        energyReadout = requireViewById(R.id.energy_readout)
        rangeReadout = requireViewById(R.id.range_readout)
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
        energySource = EnergySource.NONE
        energyCapacityCached = 0f
    }

    private fun subscribeToProperties() {
        val manager = carPropertyManager ?: return

        // Always-present: speed, gear, range.
        subscribe(manager, VehiclePropertyIds.GEAR_SELECTION)
        subscribe(manager, VehiclePropertyIds.PERF_VEHICLE_SPEED)
        subscribe(manager, VehiclePropertyIds.RANGE_REMAINING)

        // RPM: hide tile entirely on platforms that don't report it (e.g. EVs).
        val hasRpm = isPropertySupported(manager, VehiclePropertyIds.ENGINE_RPM)
        rpmReadout.visibility = if (hasRpm) View.VISIBLE else View.GONE
        if (hasRpm) {
            subscribe(manager, VehiclePropertyIds.ENGINE_RPM)
        }

        // Energy tile: prefer fuel when present; fall back to battery on EVs;
        // hide entirely if neither pair is available.
        configureEnergyTile(manager)
    }

    private fun configureEnergyTile(manager: CarPropertyManager) {
        val hasFuel = isPropertySupported(manager, VehiclePropertyIds.FUEL_LEVEL) &&
            isPropertySupported(manager, VehiclePropertyIds.INFO_FUEL_CAPACITY)
        val hasBattery = isPropertySupported(manager, VehiclePropertyIds.EV_BATTERY_LEVEL) &&
            isPropertySupported(manager, VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY)

        energySource = when {
            hasFuel -> EnergySource.FUEL
            hasBattery -> EnergySource.BATTERY
            else -> EnergySource.NONE
        }

        when (energySource) {
            EnergySource.FUEL -> {
                energyReadout.visibility = View.VISIBLE
                energyReadout.configure(
                    label = getString(R.string.label_fuel),
                    minValue = 0f,
                    maxValue = 100f,
                    showBar = true
                )
                energyCapacityCached = readCapacity(manager, VehiclePropertyIds.INFO_FUEL_CAPACITY)
                subscribe(manager, VehiclePropertyIds.FUEL_LEVEL)
            }
            EnergySource.BATTERY -> {
                energyReadout.visibility = View.VISIBLE
                energyReadout.configure(
                    label = getString(R.string.label_battery),
                    minValue = 0f,
                    maxValue = 100f,
                    showBar = true
                )
                energyCapacityCached = readCapacity(
                    manager,
                    VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY
                )
                subscribe(manager, VehiclePropertyIds.EV_BATTERY_LEVEL)
            }
            EnergySource.NONE -> {
                energyReadout.visibility = View.GONE
                energyCapacityCached = 0f
            }
        }
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

    private fun isPropertySupported(manager: CarPropertyManager, propId: Int): Boolean {
        return try {
            manager.getCarPropertyConfig(propId) != null
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun updateValue(propId: Int, value: CarPropertyValue<*>) {
        if (value.status != CarPropertyValue.STATUS_AVAILABLE) {
            return
        }

        when (propId) {
            VehiclePropertyIds.GEAR_SELECTION -> {
                val raw = value.value as? Number ?: return
                speedGauge.setBadge(formatGear(raw.toInt()))
            }
            VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                val raw = value.value as? Number ?: return
                speedGauge.setValue(convertSpeed(raw.toFloat()))
            }
            VehiclePropertyIds.RANGE_REMAINING -> {
                val raw = value.value as? Number ?: return
                rangeReadout.setValue(convertRange(raw.toFloat()))
            }
            VehiclePropertyIds.ENGINE_RPM -> {
                val raw = value.value as? Number ?: return
                rpmReadout.setValue(raw.toFloat())
            }
            VehiclePropertyIds.FUEL_LEVEL -> {
                if (energySource != EnergySource.FUEL) return
                updateEnergyPercent(value)
            }
            VehiclePropertyIds.EV_BATTERY_LEVEL -> {
                if (energySource != EnergySource.BATTERY) return
                updateEnergyPercent(value)
            }
        }
    }

    private fun updateEnergyPercent(value: CarPropertyValue<*>) {
        val raw = value.value as? Number ?: return
        if (energyCapacityCached > 0f) {
            energyReadout.setValue(raw.toFloat() / energyCapacityCached * 100f)
        }
    }

    private fun configureStaticViews() {
        val imperial = isImperialLocale()
        speedGauge.configure(
            if (imperial) "mph" else "km/h",
            0f,
            if (imperial) MAX_SPEED_MPH else MAX_SPEED_KMH
        )
        rpmReadout.configure(
            label = getString(R.string.label_rpm),
            minValue = 0f,
            maxValue = MAX_RPM,
            showBar = false
        )
        rangeReadout.configure(
            label = getString(R.string.label_range),
            minValue = 0f,
            maxValue = if (imperial) MAX_RANGE_MI else MAX_RANGE_KM,
            showBar = false
        )
        // The energy tile (fuel vs. battery) is configured dynamically in
        // configureEnergyTile() once we know what the HAL supports.
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

    private fun readCapacity(manager: CarPropertyManager, propId: Int): Float {
        val capacityValue = try {
            manager.getProperty<Float>(propId, 0)
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission for property: $propId", e)
            return 0f
        } ?: return 0f

        if (capacityValue.status != CarPropertyValue.STATUS_AVAILABLE) {
            return 0f
        }
        return (capacityValue.value as? Number)?.toFloat() ?: 0f
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
