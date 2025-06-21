package com.example.activizer

object ServerAddresses {
    // Database server (Laptop)(Flask)
    const val DatabaseAddress = "192.168.137.1:5000" //real server IP
   // const val DatabaseAddress = "10.0.2.2:5000" //for emulator + local server
    //const val DatabaseAddress = "127.0.0.1:5000" //telnet maybe??

    // Rig server (Raspberry Pi)(Flask)
    //const val RaspberryPiAddress = "192.168.137.110:5000" // real RaspPi address
    const val RaspberryPiAddress = "10.0.2.2:8081" // netsh port forwarding from emulator

}