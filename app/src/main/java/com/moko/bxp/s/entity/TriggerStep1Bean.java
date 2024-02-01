package com.moko.bxp.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: jun.liu
 * @date: 2024/1/31 15:48
 * @des:
 */
public class TriggerStep1Bean implements Parcelable {
    public boolean advBeforeTrigger;
    public int frameType;
    public String namespaceId;
    public String instanceId;
    public int urlScheme;
    public String url;
    public int major;
    public int minor;
    public String uuid;
    public String deviceName;
    public String tagId;
    public boolean isLowPowerMode;
    public int advInterval;
    public int advDuration;
    public int standByDuration;
    public int rssi;
    public int txPower;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.advBeforeTrigger ? (byte) 1 : (byte) 0);
        dest.writeInt(this.frameType);
        dest.writeString(this.namespaceId);
        dest.writeString(this.instanceId);
        dest.writeInt(this.urlScheme);
        dest.writeString(this.url);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeString(this.uuid);
        dest.writeString(this.deviceName);
        dest.writeString(this.tagId);
        dest.writeByte(this.isLowPowerMode ? (byte) 1 : (byte) 0);
        dest.writeInt(this.advInterval);
        dest.writeInt(this.advDuration);
        dest.writeInt(this.standByDuration);
        dest.writeInt(this.rssi);
        dest.writeInt(this.txPower);
    }

    public void readFromParcel(Parcel source) {
        this.advBeforeTrigger = source.readByte() != 0;
        this.frameType = source.readInt();
        this.namespaceId = source.readString();
        this.instanceId = source.readString();
        this.urlScheme = source.readInt();
        this.url = source.readString();
        this.major = source.readInt();
        this.minor = source.readInt();
        this.uuid = source.readString();
        this.deviceName = source.readString();
        this.tagId = source.readString();
        this.isLowPowerMode = source.readByte() != 0;
        this.advInterval = source.readInt();
        this.advDuration = source.readInt();
        this.standByDuration = source.readInt();
        this.rssi = source.readInt();
        this.txPower = source.readInt();
    }

    public TriggerStep1Bean() {
    }

    protected TriggerStep1Bean(Parcel in) {
        this.advBeforeTrigger = in.readByte() != 0;
        this.frameType = in.readInt();
        this.namespaceId = in.readString();
        this.instanceId = in.readString();
        this.urlScheme = in.readInt();
        this.url = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.uuid = in.readString();
        this.deviceName = in.readString();
        this.tagId = in.readString();
        this.isLowPowerMode = in.readByte() != 0;
        this.advInterval = in.readInt();
        this.advDuration = in.readInt();
        this.standByDuration = in.readInt();
        this.rssi = in.readInt();
        this.txPower = in.readInt();
    }

    public static final Creator<TriggerStep1Bean> CREATOR = new Creator<TriggerStep1Bean>() {
        @Override
        public TriggerStep1Bean createFromParcel(Parcel source) {
            return new TriggerStep1Bean(source);
        }

        @Override
        public TriggerStep1Bean[] newArray(int size) {
            return new TriggerStep1Bean[size];
        }
    };
}
