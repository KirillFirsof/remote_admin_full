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

    public SystemMonitor() {
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.os = systemInfo.getOperatingSystem();
    }

    public String getComputerName() {
        return os.getNetworkParams().getHostName();
    }

    private double lastCpuLoad = 0.0;

    public double getCpuLoad() {
        CentralProcessor processor = hardware.getProcessor();
        
        // Получаем нагрузку за последнюю секунду
        double load = processor.getSystemCpuLoad(1000) * 100;
        
        // Если нагрузка 0.0 или отрицательная, пробуем другой метод
        if (load <= 0.0) {
            load = processor.getSystemLoadAverage(1)[0] * 100;
        }
        
        // Если все еще 0, берем последнее известное значение
        if (load <= 0.0 && lastCpuLoad > 0) {
            return lastCpuLoad;
        }
        
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