package com.fbz.performancetest.util;

import java.text.SimpleDateFormat;
import java.util.*;

public class MemoryMonitor extends Monitor{

    private final Device device;

    private final String packageName;
    private final ProcessUtil processUtil = new ProcessUtil();
    private ArrayList<Map<String, String>> memoryResult = new ArrayList<>();


    private boolean isRun = false;

    public MemoryMonitor(Device device, String packageName) {
        this.device = device;
        this.packageName = packageName;
    }

    public String getMemory() {
        String memoryInfo = processUtil.getBack(device.adbShell(String.format("dumpsys meminfo %s | grep -w -A12 'App Summary'", packageName)));
        String[] infoArr = memoryInfo.replace("\r", "").replace("\n", "").split(" ");
        infoArr = Arrays.stream(infoArr).filter(x -> !"".equals(x)).toArray(String[]::new);
        System.out.println(Arrays.toString(infoArr));
        if(infoArr.length != 40){
            throw new RuntimeException("memory get err");
        }
        return infoArr[infoArr.length - 8];
    }

    public void realGetMemoryInfo() {
        System.out.println("join");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (isRun) {
            String memoryItemInfo = getMemory();
            Map<String, String> itemResultMap = new HashMap<>();
            itemResultMap.put("time", dateFormat.format(new Date()));
            itemResultMap.put("cpu", memoryItemInfo);
            memoryResult.add(itemResultMap);
            System.out.println(memoryResult);
        }

    }

    @Override
    public boolean start() {
        isRun = true;
        Thread thread = new Thread(() -> {
            realGetMemoryInfo();
        });
        thread.start();
        return true;
    }

    @Override
    public boolean stop() {
        isRun = false;
        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        Device device = new Device("10.130.131.79", 5039, "e03c55d0");
        String apk = "com.happyelements.AndroidAnimal";
        device.startApk(apk);
        if(device.apkIsStart(apk)){
            MemoryMonitor memoryMonitor = new MemoryMonitor(device, apk);
            memoryMonitor.start();
            Thread.sleep(10000);
            memoryMonitor.stop();
        }
    }
}
