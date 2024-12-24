package com.moko.bxp.s.utils;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.s.entity.THStoreData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author: jun.liu
 * @date: 2024/12/2 19:50
 * @des:
 */
public class ExcelHelper {
    private WritableWorkbook wwb;
    private final String TIME = "Time";
    private final String TEMPERATURE = "Temperature";
    private final String HUMIDITY = "Humidity";
    private final String MAC = "MAC Address";
    private final String SAMPLING_INTERVAL = "Sampling interval";
    private final String STORAGE_INTERVAL = "Storage interval";
    private final String START_TIME = "Start Time";
    private final String END_TIME = "End Time";
    private final String TOTAL_DURATION_RECORDED = "Total Duration Recorded";
    private final String TOTAL_DATA_RECORDED = "Total Data Recorded";
    private final String MAX_TEMPERATURE = "Max Temperature";
    private final String MIN_TEMPERATURE = "Min Temperature";
    private final String AVERAGE_TEMPERATURE = "Average Temperature";
    private final String MAX_HUMIDITY = "Max Humidity";
    private final String MIN_HUMIDITY = "Min Humidity";
    private final String AVERAGE_HUMIDITY = "Average Humidity";

    /**
     * 创建数据表
     *
     * @param path 文件路径
     * @throws Exception 抛出异常
     */
    public File createExcel(String path) throws Exception {
        File file = new File(path);
        if (file.exists()) {
            boolean a = file.delete();
            XLog.i(a);
        }
        wwb = Workbook.createWorkbook(file);//创建表
        WritableSheet ws1 = wwb.createSheet("Logger Data Records", 0);//表名 页数
        WritableSheet ws2 = wwb.createSheet("Logger Summary", 1);//表名 页数
        Label lbl0 = new Label(0, 0, TIME);
        Label lbl1 = new Label(1, 0, "℃/℉");
        Label lbl2 = new Label(2, 0, "%RH");

        Label lbl3 = new Label(0, 0, MAC);
        Label lbl4 = new Label(0, 1, SAMPLING_INTERVAL);
        Label lbl5 = new Label(0, 2, STORAGE_INTERVAL);
        Label lbl6 = new Label(0, 3, START_TIME);
        Label lbl7 = new Label(0, 4, END_TIME);
        Label lbl8 = new Label(0, 5, TOTAL_DURATION_RECORDED);
        Label lbl9 = new Label(0, 6, TOTAL_DATA_RECORDED);
        Label lbl10 = new Label(0, 7, MAX_TEMPERATURE);
        Label lbl11 = new Label(0, 8, MIN_TEMPERATURE);
        Label lbl12 = new Label(0, 9, AVERAGE_TEMPERATURE);
        Label lbl13 = new Label(0, 10, MAX_HUMIDITY);
        Label lbl14 = new Label(0, 11, MIN_HUMIDITY);
        Label lbl15 = new Label(0, 12, AVERAGE_HUMIDITY);

        ws1.addCell(lbl0);
        ws1.addCell(lbl1);
        ws1.addCell(lbl2);

        ws2.addCell(lbl3);
        ws2.addCell(lbl4);
        ws2.addCell(lbl5);
        ws2.addCell(lbl6);
        ws2.addCell(lbl7);
        ws2.addCell(lbl8);
        ws2.addCell(lbl9);
        ws2.addCell(lbl10);
        ws2.addCell(lbl11);
        ws2.addCell(lbl12);
        ws2.addCell(lbl13);
        ws2.addCell(lbl14);
        ws2.addCell(lbl15);

        // 从内存中写入文件中
        wwb.write();
        wwb.close();
        return file;
    }

    /**
     * 向数据表写入数据
     *
     * @param lsRecord 插入的数据
     * @param file     文件
     * @throws Exception 抛出异常
     */
    public void writeToExcel(List<Map<String, String>> lsRecord, Map<String, String> lsTotal, File file) throws Exception {
        Workbook oldWwb = Workbook.getWorkbook(file);
        wwb = Workbook.createWorkbook(file, oldWwb);
        //Logger Data Records
        WritableSheet ws1 = wwb.getSheet(0);
        for (int i = 0; i < lsRecord.size(); i++) {
            int row = ws1.getRows();
            Label lab0 = new Label(0, row, lsRecord.get(i).get(TIME));
            Label lab1 = new Label(1, row, lsRecord.get(i).get(TEMPERATURE));
            Label lab2 = new Label(2, row, lsRecord.get(i).get(HUMIDITY));

            ws1.addCell(lab0);
            ws1.addCell(lab1);
            ws1.addCell(lab2);
        }
        //total records
        WritableSheet ws2 = wwb.getSheet(1);
//        WritableCellFormat format = new WritableCellFormat();
//        format.setAlignment(Alignment.LEFT);
//        format.setWrap(true);
        Label label3 = new Label(1, 0, lsTotal.get(MAC));
        Label label4 = new Label(1, 1, lsTotal.get(SAMPLING_INTERVAL));
        Label label5 = new Label(1, 2, lsTotal.get(STORAGE_INTERVAL));
        Label label6 = new Label(1, 3, lsTotal.get(START_TIME));
        Label label7 = new Label(1, 4, lsTotal.get(END_TIME));
        Label label8 = new Label(1, 5, lsTotal.get(TOTAL_DURATION_RECORDED));
        Label label9 = new Label(1, 6, lsTotal.get(TOTAL_DATA_RECORDED));
        Label label10 = new Label(1, 7, lsTotal.get(MAX_TEMPERATURE));
        Label label11 = new Label(1, 8, lsTotal.get(MIN_TEMPERATURE));
        Label label12 = new Label(1, 9, lsTotal.get(AVERAGE_TEMPERATURE));
        Label label13 = new Label(1, 10, lsTotal.get(MAX_HUMIDITY));
        Label label14 = new Label(1, 11, lsTotal.get(MIN_HUMIDITY));
        Label label15 = new Label(1, 12, lsTotal.get(AVERAGE_HUMIDITY));

        ws2.addCell(label3);
        ws2.addCell(label4);
        ws2.addCell(label5);
        ws2.addCell(label6);
        ws2.addCell(label7);
        ws2.addCell(label8);
        ws2.addCell(label9);
        ws2.addCell(label10);
        ws2.addCell(label11);
        ws2.addCell(label12);
        ws2.addCell(label13);
        ws2.addCell(label14);
        ws2.addCell(label15);
        // 从内存中写入文件中,只能刷一次
        wwb.write();
        wwb.close();
    }

    public List<Map<String, String>> handleLoggerData(@NonNull List<THStoreData> dataSource) {
        List<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < dataSource.size(); i++) {
            Map<String, String> map = new HashMap<>(8);
            THStoreData info = dataSource.get(i);
            map.put(TIME, info.time);
            map.put(TEMPERATURE, getFormatTemp(info.intTemp));
            map.put(HUMIDITY, info.humidity);
            data.add(map);
        }
        return data;
    }

    public Map<String, String> handleTotalData(String mac, int samplingInterval, int storageInterval, boolean isOnlyTemp, @NonNull List<THStoreData> data) {
        String startTime = data.get(data.size() - 1).time;
        String endTime = data.get(0).time;
        String totalDurationRecorded = formatTime(data.get(0).timeStamp - data.get(data.size() - 1).timeStamp);
        String totalDataRecorded = String.valueOf(data.size());
        String[] tempArray = getTemperature(data);
        Map<String, String> map = new HashMap<>(32);
        map.put(MAC, mac);
        map.put(SAMPLING_INTERVAL, samplingInterval + " s");
        map.put(STORAGE_INTERVAL, storageInterval + " min");
        map.put(START_TIME, startTime);
        map.put(END_TIME, endTime);
        map.put(TOTAL_DURATION_RECORDED, totalDurationRecorded);
        map.put(TOTAL_DATA_RECORDED, totalDataRecorded);
        map.put(MAX_TEMPERATURE, tempArray[0]);
        map.put(MIN_TEMPERATURE, tempArray[1]);
        map.put(AVERAGE_TEMPERATURE, tempArray[2]);
        if (!isOnlyTemp) {
            String[] humArray = getHumidity(data);
            map.put(MAX_HUMIDITY, humArray[0]);
            map.put(MIN_HUMIDITY, humArray[1]);
            map.put(AVERAGE_HUMIDITY, humArray[2]);
        }
        return map;
    }

    public String[] getHumidity(@NonNull List<THStoreData> dataSource) {
        List<THStoreData> data = new LinkedList<>(dataSource);
        String[] array = new String[3];
        //升序排列
        Collections.sort(data, (data1, data2) -> data1.intHumidity - data2.intHumidity);
        //最大值
        array[0] = MokoUtils.getDecimalFormat("0.0").format(data.get(data.size() - 1).intHumidity * 0.1) + "【" + data.get(data.size() - 1).time+"】";
        //最小值
        array[1] = MokoUtils.getDecimalFormat("0.0").format(data.get(0).intHumidity * 0.1) + "【" + data.get(0).time+"】";
        int total = 0;
        for (THStoreData thStoreData : data) {
            total += thStoreData.intHumidity;
        }
        //求平均值
        array[2] = MokoUtils.getDecimalFormat("0.0").format(total / (data.size() * 10.0));
        return array;
    }

    private String[] getTemperature(@NonNull List<THStoreData> data) {
        List<THStoreData> sortData = new LinkedList<>(data);
        String[] array = new String[3];
        //升序排列
        Collections.sort(sortData, (data1, data2) -> data1.intTemp - data2.intTemp);
        //最大值
        array[0] = getFormatTemp(sortData.get(sortData.size() - 1).intTemp) + "【" + sortData.get(sortData.size() - 1).time+"】";
        //最小值
        array[1] = getFormatTemp(sortData.get(0).intTemp) + "【" + sortData.get(0).time+"】";
        int total = 0;
        for (THStoreData thStoreData : sortData) {
            total += thStoreData.intTemp;
        }
        //求平均值
        array[2] = getFormatTemp(total / (sortData.size() * 1.0));
        return array;
    }

    private String getFormatTemp(double temperature) {
        double tempF = 1.8 * temperature + 32 * 10;
        String tempStr = MokoUtils.getDecimalFormat("0.0").format(temperature * 0.1);
        String tempFStr = MokoUtils.getDecimalFormat("0.0").format(tempF * 0.1);
        return tempStr + " / " + tempFStr;
    }

    private String formatTime(long formatTime) {
        long time = formatTime / 1000;
        long day = TimeUnit.SECONDS.toDays(time);
        long hours = TimeUnit.SECONDS.toHours(time) - TimeUnit.DAYS.toHours(TimeUnit.SECONDS.toDays(time));
        long minutes = TimeUnit.SECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(time));
        long seconds = TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time));
        if (day != 0) return day + "day" + hours + "Hrs" + minutes + "Mins" + seconds + "Sec";
        if (hours != 0) return hours + "Hrs" + minutes + "Mins" + seconds + "Sec";
        if (minutes != 0) return minutes + "Mins" + seconds + "Sec";
        return seconds + "Sec";
    }
}
