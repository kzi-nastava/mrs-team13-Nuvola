package com.example.nuvola.network;

public class ServerConfig {

    /**
     * Host of the backend server.
     * - "10.0.2.2" only works from the Android EMULATOR (alias for the host machine's localhost).
     * - When running on a PHYSICAL device (e.g. over Wi-Fi), set this to your computer's
     *   LAN IP address (find it with `ipconfig` on Windows / `ifconfig` on Mac/Linux), and make
     *   sure the phone and computer are on the same network.
     */
    public static final String HOST = "192.168.1.122";
    public static final int PORT = 8080;

    public static final String BASE_URL = "http://" + HOST + ":" + PORT + "/";
    public static final String WS_URL = "ws://" + HOST + ":" + PORT + "/ws-native";
}