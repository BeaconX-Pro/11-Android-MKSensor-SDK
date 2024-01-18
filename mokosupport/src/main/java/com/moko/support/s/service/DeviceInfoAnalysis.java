package com.moko.support.s.service;

import com.moko.support.s.entity.DeviceInfo;

public interface DeviceInfoAnalysis<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
