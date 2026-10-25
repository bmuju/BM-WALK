package com.example.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.example.data.model.BleConnectionState
import com.example.data.model.BleDeviceItem
import com.example.data.model.HeartRateZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class BleHeartRateManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null
    private var scanJob: Job? = null
    private var demoJob: Job? = null

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _currentHeartRate = MutableStateFlow<Int?>(null)
    val currentHeartRate: StateFlow<Int?> = _currentHeartRate.asStateFlow()

    private val _heartRateZone = MutableStateFlow<HeartRateZone?>(null)
    val heartRateZone: StateFlow<HeartRateZone?> = _heartRateZone.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceItem>> = _scannedDevices.asStateFlow()

    private val _isDemoMode = MutableStateFlow(false)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val heartRateReadings = mutableListOf<Int>()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: result.scanRecord?.deviceName ?: "HR Sensor (${device.address.takeLast(5)})"
            val address = device.address ?: return
            val rssi = result.rssi

            val current = _scannedDevices.value
            if (current.none { it.address == address }) {
                _scannedDevices.value = current + BleDeviceItem(name, address, rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleHeartRateManager", "BLE scan failed with error code $errorCode")
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = BleConnectionState.CONNECTING
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnect()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    if (descriptor != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        } else {
                            @Suppress("DEPRECATION")
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                    _connectionState.value = BleConnectionState.CONNECTED
                    _connectedDeviceName.value = gatt.device.name ?: "Heart Rate Monitor"
                } else {
                    Log.w("BleHeartRateManager", "HR Measurement characteristic not found")
                }
            }
        }

        @Deprecated("Deprecated for API 33+")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                @Suppress("DEPRECATION")
                val value = characteristic.value ?: return
                parseHeartRatePacket(value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                parseHeartRatePacket(value)
            }
        }
    }

    private fun parseHeartRatePacket(value: ByteArray) {
        if (value.isEmpty()) return
        val flags = value[0].toInt()
        val is16Bit = (flags and 0x01) != 0

        val bpm = if (is16Bit) {
            if (value.size >= 3) {
                ((value[2].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
            } else 0
        } else {
            if (value.size >= 2) {
                value[1].toInt() and 0xFF
            } else 0
        }

        if (bpm > 30 && bpm < 250) {
            recordBpm(bpm)
        }
    }

    private fun recordBpm(bpm: Int) {
        scope.launch {
            _currentHeartRate.value = bpm
            _heartRateZone.value = HeartRateZone.fromBpm(bpm)
            heartRateReadings.add(bpm)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        stopDemo()
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null || !isBluetoothEnabled) {
            _connectionState.value = BleConnectionState.DISCONNECTED
            return
        }

        _scannedDevices.value = emptyList()
        _connectionState.value = BleConnectionState.SCANNING

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            // Also scan without filter as fallback for sensors that do not advertise service UUID in packet header
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            try {
                scanner.startScan(scanCallback)
            } catch (err: Exception) {
                Log.e("BleHeartRateManager", "Cannot start scan", err)
            }
        }

        // Auto-stop scan after 15 seconds to conserve battery
        scanJob?.cancel()
        scanJob = scope.launch {
            delay(15000L)
            if (_connectionState.value == BleConnectionState.SCANNING) {
                stopScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanJob?.cancel()
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e("BleHeartRateManager", "Error stopping scan", e)
        }
        if (_connectionState.value == BleConnectionState.SCANNING) {
            _connectionState.value = BleConnectionState.DISCONNECTED
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        stopScan()
        stopDemo()
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        _connectionState.value = BleConnectionState.CONNECTING
        _connectedDeviceName.value = device.name ?: "HR Monitor"

        bluetoothGatt?.close()
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    fun startDemoSensor() {
        disconnect()
        _isDemoMode.value = true
        _connectionState.value = BleConnectionState.CONNECTED
        _connectedDeviceName.value = "Virtual BLE HRM (Demo)"

        demoJob?.cancel()
        demoJob = scope.launch {
            var simulatedBpm = 118
            while (isActive) {
                // Realistic pulse variation during brisk walking
                val delta = (-2..3).random()
                simulatedBpm = (simulatedBpm + delta).coerceIn(105, 148)
                recordBpm(simulatedBpm)
                delay(1000L)
            }
        }
    }

    fun stopDemo() {
        demoJob?.cancel()
        _isDemoMode.value = false
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        stopDemo()
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.e("BleHeartRateManager", "Error closing GATT", e)
        }
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
        _connectedDeviceName.value = null
        _currentHeartRate.value = null
        _heartRateZone.value = null
    }

    fun resetStats() {
        heartRateReadings.clear()
        _currentHeartRate.value = null
        _heartRateZone.value = null
    }

    fun getAverageHeartRate(): Int {
        if (heartRateReadings.isEmpty()) return 0
        return heartRateReadings.average().toInt()
    }

    fun getMaxHeartRate(): Int {
        return heartRateReadings.maxOrNull() ?: 0
    }

    companion object {
        val HEART_RATE_SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        @Volatile
        private var INSTANCE: BleHeartRateManager? = null

        fun getInstance(context: Context): BleHeartRateManager {
            return INSTANCE ?: synchronized(this) {
                val instance = BleHeartRateManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
