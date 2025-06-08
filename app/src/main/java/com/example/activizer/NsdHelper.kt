package com.example.activizer

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import java.net.InetAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NsdHelper(private val context: Context) {
    private val TAG = "NsdHelper"
    private val SERVICE_TYPE = "_activizer._tcp."
    private val SERVICE_NAME = "Activizer"
    private val LOCALHOST = "127.0.0.1"
    private val DEFAULT_PORT = 5000
    
    private var nsdManager: NsdManager? = null
    private var serviceInfo: NsdServiceInfo? = null
    private var hostAddress: String? = null
    private var hostPort: Int? = null
    private var discoveryCallback: DiscoveryCallback? = null

    interface DiscoveryCallback {
        fun onServiceDiscovered(address: String, port: Int)
        fun onDiscoveryFailed(errorCode: Int)
    }

    fun setDiscoveryCallback(callback: DiscoveryCallback) {
        discoveryCallback = callback
    }
    
    private val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service registered: $serviceInfo")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Registration failed: $errorCode")
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service unregistered: $serviceInfo")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Unregistration failed: $errorCode")
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed to start: $errorCode")
            fallbackToLocalhost()
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed to stop: $errorCode")
            fallbackToLocalhost()
        }

        override fun onDiscoveryStarted(serviceType: String) {
            Log.d(TAG, "Discovery started")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.d(TAG, "Discovery stopped")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service found: $service")
            if (service.serviceName == SERVICE_NAME) {
                resolveService(service)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.d(TAG, "Service lost: $service")
            if (service.serviceName == SERVICE_NAME) {
                hostAddress = null
                hostPort = null
            }
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.e(TAG, "Resolve failed: $errorCode")
            fallbackToLocalhost()
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.d(TAG, "Service resolved: $serviceInfo")
            hostAddress = serviceInfo.host.hostAddress
            hostPort = serviceInfo.port
            Log.d(TAG, "Host address: $hostAddress, Port: $hostPort")
            
            hostAddress?.let { address ->
                hostPort?.let { port ->
                    discoveryCallback?.onServiceDiscovered(address, port)
                }
            }
        }
    }

    suspend fun initialize() {
        // First try connecting to localhost
        if (tryConnectToLocalhost()) {
            hostAddress = LOCALHOST
            hostPort = DEFAULT_PORT
            discoveryCallback?.onServiceDiscovered(LOCALHOST, DEFAULT_PORT)
            return
        }

        // If localhost connection fails, start network service discovery
        startNetworkDiscovery()
    }

    private suspend fun tryConnectToLocalhost(): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket(LOCALHOST, DEFAULT_PORT).use { socket ->
                socket.isConnected
            }
        } catch (e: Exception) {
            Log.d(TAG, "Failed to connect to localhost: ${e.message}")
            false
        }
    }

    private fun startNetworkDiscovery() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        discoverServices()
    }

    private fun discoverServices() {
        nsdManager?.discoverServices(
            SERVICE_TYPE,
            NsdManager.PROTOCOL_DNS_SD,
            discoveryListener
        )
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, resolveListener)
    }

    fun getHostAddress(): String? = hostAddress
    fun getHostPort(): Int? = hostPort

    fun tearDown() {
        try {
            nsdManager?.stopServiceDiscovery(discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service discovery: ${e.message}")
        }
    }

    private fun fallbackToLocalhost() {
        hostAddress = LOCALHOST
        hostPort = DEFAULT_PORT
        discoveryCallback?.onServiceDiscovered(LOCALHOST, DEFAULT_PORT)
    }
} 