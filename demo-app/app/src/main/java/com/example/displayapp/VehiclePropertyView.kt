package com.example.displayapp

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.text.InputType
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView

class VehiclePropertyView : RelativeLayout {
    private lateinit var mContext: Context
    private lateinit var mId: TextView
    private lateinit  var mName: TextView
    private lateinit var mValue: TextView
    private lateinit  var mEditButton: Button

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context!!
        inflate(context, R.layout.vehicle_property_view, this)
        mId = findViewById(R.id.vehicle_property_id)
        mId.text = ""
        mName = findViewById(R.id.vehicle_property_name)
        mName.text = ""
        mValue = findViewById(R.id.vehicle_property_value)
        mValue.text = ""
        mEditButton = findViewById(R.id.vehicle_property_set)
    }

    fun setPropId(id: Int): VehiclePropertyView {
        mId.text = id.toString()
        return this
    }

    fun setPropName(text: String?): VehiclePropertyView {
        mName.text = text
        return this
    }

    fun setPropValue(text: String?): VehiclePropertyView {
        mValue.text = text
        return this
    }

    fun enableSetValue(cb: (String) -> Any): VehiclePropertyView {
        mEditButton.isEnabled = true
        mEditButton.setOnClickListener {
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("Set '" + mName.text + "' vehicle property value to:")
            val input = EditText(mContext)
            input.setText(mValue.text)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            builder.setView(input)
            builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
                cb(input.text.toString())
            }
            builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            builder.show()
        }
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}