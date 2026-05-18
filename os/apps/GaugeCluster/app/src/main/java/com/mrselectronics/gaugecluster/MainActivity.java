package com.mrselectronics.gaugecluster;

import android.car.Car;
import android.car.VehiclePropertyIds;
import android.car.hardware.CarPropertyValue;
import android.car.hardware.property.CarPropertyManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GaugeCluster";
    private static final float METERS_PER_SECOND_TO_MPH = 2.2369363f;
    private static final float METERS_PER_SECOND_TO_KMH = 3.6f;
    private static final float METERS_PER_MILE = 1609.344f;
    private static final float METERS_PER_KILOMETER = 1000f;
    private static final float MAX_SPEED_MPH = 120f;
    private static final float MAX_SPEED_KMH = 200f;
    private static final float MAX_RANGE_MI = 300f;
    private static final float MAX_RANGE_KM = 500f;
    private static final float MAX_RPM = 7000f;

    private TextView mGearText;
    private GaugeView mFuelGauge;
    private GaugeView mSpeedGauge;
    private GaugeView mRangeGauge;
    private GaugeView mRpmGauge;

    private Car mCar;
    private CarPropertyManager mCarPropertyManager;
    private Locale mLocale;

    private final CarPropertyManager.CarPropertyEventCallback mCarPropertyCallback =
            new CarPropertyManager.CarPropertyEventCallback() {
                @Override
                public void onChangeEvent(CarPropertyValue value) {
                    runOnUiThread(() -> updateValue(value.getPropertyId(), value));
                }

                @Override
                public void onErrorEvent(int propId, int zone) {
                    Log.e(TAG, "Error for property: " + propId + " zone: " + zone);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGearText = findViewById(R.id.gear_value);
        mFuelGauge = findViewById(R.id.fuel_gauge);
        mSpeedGauge = findViewById(R.id.speed_gauge);
        mRangeGauge = findViewById(R.id.range_gauge);
        mRpmGauge = findViewById(R.id.rpm_gauge);
        mLocale = getResources().getConfiguration().getLocales().get(0);
        configureGauges();

        mCar = Car.createCar(this);
        mCarPropertyManager = (CarPropertyManager) mCar.getCarManager(Car.PROPERTY_SERVICE);
        if (mCarPropertyManager != null) {
            subscribeToProperties();
        } else {
            Log.e(TAG, "Car property manager unavailable");
        }
    }

    private void subscribeToProperties() {
        subscribe(VehiclePropertyIds.GEAR_SELECTION);
        subscribe(VehiclePropertyIds.FUEL_LEVEL);
        subscribe(VehiclePropertyIds.PERF_VEHICLE_SPEED);
        subscribe(VehiclePropertyIds.RANGE_REMAINING);
        subscribe(VehiclePropertyIds.ENGINE_RPM);
    }

    private void subscribe(int propId) {
        try {
            mCarPropertyManager.registerCallback(mCarPropertyCallback, propId,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE);
        } catch (SecurityException e) {
            Log.e(TAG, "No permission for property: " + propId, e);
        }
    }

    private void updateValue(int propId, CarPropertyValue value) {
        if (value.getStatus() != CarPropertyValue.STATUS_AVAILABLE) {
            return;
        }

        Number rawValue = (Number) value.getValue();
        switch (propId) {
            case VehiclePropertyIds.GEAR_SELECTION:
                mGearText.setText(formatGear(rawValue.intValue()));
                break;
            case VehiclePropertyIds.FUEL_LEVEL:
                float fuelCapacity = getFuelCapacity();
                if (fuelCapacity > 0) {
                    float fuelPercent = rawValue.floatValue() / fuelCapacity * 100f;
                    mFuelGauge.setValue(fuelPercent);
                }
                break;
            case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                mSpeedGauge.setValue(convertSpeed(rawValue.floatValue()));
                break;
            case VehiclePropertyIds.RANGE_REMAINING:
                mRangeGauge.setValue(convertRange(rawValue.floatValue()));
                break;
            case VehiclePropertyIds.ENGINE_RPM:
                mRpmGauge.setValue(rawValue.floatValue());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCarPropertyManager != null) {
            mCarPropertyManager.unregisterCallback(mCarPropertyCallback);
        }
        if (mCar != null) {
            mCar.disconnect();
        }
    }

    private boolean isImperialLocale() {
        String country = mLocale.getCountry();
        return "US".equals(country) || "LR".equals(country) || "MM".equals(country);
    }

    private void configureGauges() {
        boolean imperial = isImperialLocale();
        mSpeedGauge.configure(
                getString(R.string.label_speed),
                imperial ? "mph" : "km/h",
                0f,
                imperial ? MAX_SPEED_MPH : MAX_SPEED_KMH);
        mRpmGauge.configure(getString(R.string.label_rpm), "rpm", 0f, MAX_RPM);
        mFuelGauge.configure(getString(R.string.label_fuel), "%", 0f, 100f);
        mRangeGauge.configure(
                getString(R.string.label_range),
                imperial ? "mi" : "km",
                0f,
                imperial ? MAX_RANGE_MI : MAX_RANGE_KM);
    }

    private float convertSpeed(float metersPerSecond) {
        if (isImperialLocale()) {
            return metersPerSecond * METERS_PER_SECOND_TO_MPH;
        }
        return metersPerSecond * METERS_PER_SECOND_TO_KMH;
    }

    private float convertRange(float meters) {
        if (isImperialLocale()) {
            return meters / METERS_PER_MILE;
        }
        return meters / METERS_PER_KILOMETER;
    }

    private float getFuelCapacity() {
        CarPropertyValue fuelCapacityValue = mCarPropertyManager.getProperty(
                VehiclePropertyIds.INFO_FUEL_CAPACITY, 0);
        if (fuelCapacityValue == null || fuelCapacityValue.getStatus()
                != CarPropertyValue.STATUS_AVAILABLE) {
            return 0f;
        }
        return ((Number) fuelCapacityValue.getValue()).floatValue();
    }

    private String formatGear(int gearValue) {
        switch (gearValue) {
            case 1:
                return "N";
            case 2:
                return "R";
            case 4:
                return "P";
            case 8:
                return "D";
            default:
                return Integer.toString(gearValue);
        }
    }
}
