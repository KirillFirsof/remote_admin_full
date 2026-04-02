package com.example.agent;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class SystemMonitor {
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    private final OperatingSystem os;
    private long[] prevTicks = null;
    private double lastCpuLoad = 0.0;

    public SystemMonitor() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
    }

    public String getComputerName() {
        return os.getNetworkParams().getHostName();
    }


    public double getCpuLoad() {
        CentralProcessor processor = hardware.getProcessor();

        // Первый вызов — инициализация
        if (prevTicks == null) {
            prevTicks = processor.getSystemCpuLoadTicks();
            try {
                Thread.sleep(50); // Небольшая пауза для первого замера
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 0.0;
        }

        long[] currentTicks = processor.getSystemCpuLoadTicks();
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = currentTicks;

        if (load < 0) load = 0;
        lastCpuLoad = Math.round(load * 10) / 10.0;
        return lastCpuLoad;
    }

    public int getFreeRamMb() {
        GlobalMemory memory = hardware.getMemory();
        return (int) (memory.getAvailable() / (1024 * 1024));
    }

    public void printSystemInfo() {
        System.out.println("Computer name: " + getComputerName());
        System.out.println("CPU load: " + getCpuLoad() + "%");
        System.out.println("Free RAM: " + getFreeRamMb() + " MB");
    }
}