package com.mrselectronics.demoapp

import android.app.Activity
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.CarPropertyManager.CarPropertyEventCallback
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import java.util.Arrays
import kotlin.Array
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.IntArray
import kotlin.String

class MainActivity : Activity() {
    lateinit var mGearPropertyView: VehiclePropertyView
    lateinit var mSpeedPropertyView: VehiclePropertyView
    lateinit var mBatteryLevelPropertyView: VehiclePropertyView
    lateinit var mFuelDoorOpenPropertyView: VehiclePropertyView
    private lateinit var mCarPropertyManager: CarPropertyManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request dangerous permissions only
        val dangPermToRequest = checkDangerousPermissions()
        if (dangPermToRequest.isEmpty()) {
            main()
        } else {
            requestDangerousPermissions(dangPermToRequest)
            // CB:
            // onRequestPermissionsResult()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            //all permissions have been granted
            if (Arrays.stream(grantResults)
                    .noneMatch { x: Int -> x == PackageManager.PERMISSION_DENIED }
            ) {
                main()
            }
        }
    }

    private fun main() {
        initCarPropertyManager()
        initGUI()
        testGetProperties()
        registerCarPropertyManagerCBs()
    }

    private fun checkDangerousPermissions(): List<String> {
        val permissions: MutableList<String> = ArrayList()
        if (checkSelfPermission(Car.PERMISSION_SPEED) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Car.PERMISSION_SPEED)
        }
        if (checkSelfPermission(Car.PERMISSION_ENERGY) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Car.PERMISSION_ENERGY)
        }
        return permissions
    }

    private fun requestDangerousPermissions(permissions: List<String>) {
        requestPermissions(permissions.toTypedArray(), REQUEST_CODE_ASK_PERMISSIONS)
    }

    private fun initGUI() {
        Log.d(TAG, "from kotlin !!!!!!!!!!!!")
        setContentView(R.layout.activity_main)
        mGearPropertyView = findViewById(R.id.gear_property_view)
        mGearPropertyView.setPropId(VehiclePropertyIds.GEAR_SELECTION)
            .setPropName(VehiclePropertyIds.toString(VehiclePropertyIds.GEAR_SELECTION))
        mSpeedPropertyView = findViewById(R.id.speed_property_view)
        mSpeedPropertyView.setPropId(VehiclePropertyIds.PERF_VEHICLE_SPEED)
            .setPropName(VehiclePropertyIds.toString(VehiclePropertyIds.PERF_VEHICLE_SPEED))
        mBatteryLevelPropertyView = findViewById(R.id.battery_level_property_view)
        mBatteryLevelPropertyView.setPropId(VehiclePropertyIds.EV_BATTERY_LEVEL)
            .setPropName(VehiclePropertyIds.toString(VehiclePropertyIds.EV_BATTERY_LEVEL))
        mFuelDoorOpenPropertyView = findViewById(R.id.fuel_door_property_view)
        mFuelDoorOpenPropertyView.setPropId(VehiclePropertyIds.FUEL_DOOR_OPEN)
            .setPropName(VehiclePropertyIds.toString(VehiclePropertyIds.FUEL_DOOR_OPEN))
            .enableSetValue { value_to_set: String ->
                Log.d(TAG, "FUEL_DOOR_OPEN: onEdit($value_to_set)")
                try {
                    mCarPropertyManager.setBooleanProperty(
                        VehiclePropertyIds.FUEL_DOOR_OPEN,
                        0,
                        value_to_set.toBoolean()
                    )
                } catch (e: SecurityException) {
                    Log.e(TAG, "FUEL_DOOR_OPEN: setBooleanProperty(), Exception: " + e.message)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "FUEL_DOOR_OPEN: setBooleanProperty(), Exception: " + e.message)
                }
            }
    }

    private fun initCarPropertyManager() {
        mCarPropertyManager =
            Car.createCar(this)!!.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    private fun testGetProperties() {
        Log.d(TAG, "Test CarPropertyManager getters:")
        val gearSelection =
            mCarPropertyManager.getIntProperty(VehiclePropertyIds.GEAR_SELECTION, 0)
        Log.d(
            TAG,
            "GEAR_SELECTION: getIntProperty(" + VehiclePropertyIds.GEAR_SELECTION + ", 0)=" + gearSelection
        )
    }

    private fun registerCarPropertyManagerCBs() {
        Log.d(TAG, "Test CarPropertyManager callbacks:")
        mCarPropertyManager.registerCallback(object : CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                Log.d(TAG, "GEAR_SELECTION: onChangeEvent(" + carPropertyValue.value + ")")
                mGearPropertyView.setPropValue(carPropertyValue.value.toString())
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.d(TAG, "GEAR_SELECTION: onErrorEvent($propId, $zone)")
            }
        }, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_NORMAL)
        mCarPropertyManager.registerCallback(object : CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                Log.d(TAG, "PERF_VEHICLE_SPEED: onChangeEvent(" + carPropertyValue.value + ")")
                mSpeedPropertyView.setPropValue(carPropertyValue.value.toString())
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.d(TAG, "PERF_VEHICLE_SPEED: onErrorEvent($propId, $zone)")
            }
        }, VehiclePropertyIds.PERF_VEHICLE_SPEED, CarPropertyManager.SENSOR_RATE_NORMAL)
        mCarPropertyManager.registerCallback(object : CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                Log.d(TAG, "EV_BATTERY_LEVEL: onChangeEvent(" + carPropertyValue.value + ")")
                mBatteryLevelPropertyView.setPropValue(carPropertyValue.value.toString())
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.d(TAG, "EV_BATTERY_LEVEL: onErrorEvent($propId, $zone)")
            }
        }, VehiclePropertyIds.EV_BATTERY_LEVEL, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        mCarPropertyManager.registerCallback(object : CarPropertyEventCallback {
            override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
                Log.d(TAG, "FUEL_DOOR_OPEN: onChangeEvent(" + carPropertyValue.value + ")")
                mFuelDoorOpenPropertyView.setPropValue(carPropertyValue.value.toString())
            }

            override fun onErrorEvent(propId: Int, zone: Int) {
                Log.d(TAG, "FUEL_DOOR_OPEN: onErrorEvent($propId, $zone)")
            }
        }, VehiclePropertyIds.FUEL_DOOR_OPEN, CarPropertyManager.SENSOR_RATE_ONCHANGE)
    }

    companion object {
        private const val TAG = "mrs-demo"
        private const val REQUEST_CODE_ASK_PERMISSIONS = 1
    }
}
