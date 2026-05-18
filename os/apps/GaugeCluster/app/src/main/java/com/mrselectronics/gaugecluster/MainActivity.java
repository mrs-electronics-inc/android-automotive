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
    private static final float PROPERTIES_REFRESH_RATE = 5f;

    private TextView mFuelText;
    private TextView mGearText;
    private TextView mSpeedText;
    private TextView mRangeText;
    private TextView mRpmText;

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
        mFuelText = findViewById(R.id.fuel_value);
        mSpeedText = findViewById(R.id.speed_value);
        mRangeText = findViewById(R.id.range_value);
        mRpmText = findViewById(R.id.rpm_value);
        mLocale = getResources().getConfiguration().getLocales().get(0);

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

        switch (propId) {
            case VehiclePropertyIds.GEAR_SELECTION:
                mGearText.setText(formatGear(((Number) value.getValue()).intValue()));
                break;
            case VehiclePropertyIds.FUEL_LEVEL:
                float floatValue = (float) value.getValue();
                float fuelCapacity = ((Number) mCarPropertyManager.getProperty(
                        VehiclePropertyIds.INFO_FUEL_CAPACITY, 0).getValue()).floatValue();
                if (fuelCapacity > 0) {
                    int fuelPercent = Math.round(floatValue / fuelCapacity * 100f);
                    mFuelText.setText(fuelPercent + "%");
                }
                break;
            case VehiclePropertyIds.PERF_VEHICLE_SPEED:
                floatValue = (float) value.getValue();
                if (isImperialLocale()) {
                    int speed = Math.round(floatValue * 2.2369363f);
                    mSpeedText.setText(speed + " mph");
                } else {
                    int speed = Math.round(floatValue * 3.6f);
                    mSpeedText.setText(speed + " km/h");
                }
                break;
            case VehiclePropertyIds.RANGE_REMAINING:
                floatValue = (float) value.getValue();
                if (isImperialLocale()) {
                    int range = Math.round(floatValue / 1609.344f);
                    mRangeText.setText(range + " mi");
                } else {
                    int range = Math.round(floatValue / 1000f);
                    mRangeText.setText(range + " km");
                }
                break;
            case VehiclePropertyIds.ENGINE_RPM:
                mRpmText.setText(String.format("%.0f", floatValue));
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
