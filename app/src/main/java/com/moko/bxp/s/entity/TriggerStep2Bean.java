package com.moko.bxp.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: jun.liu
 * @date: 2024/1/31 15:48
 * @des:
 */
public class TriggerStep2Bean implements Parcelable {
    public int frameType;
    public int advInterval;
    public int advDuration;
    public int rssi;
    public int txPower;
    //UID
    public String namespaceId;
    public String instanceId;
    //URL
    public int urlScheme;
    public String urlContent;
    //IBeacon
    public int major;
    public int minor;
    public String uuid;
    //sensor info
    public String deviceName;
    public String tagId;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.frameType);
        dest.writeInt(this.advInterval);
        dest.writeInt(this.advDuration);
        dest.writeInt(this.rssi);
        dest.writeInt(this.txPower);
        dest.writeString(this.namespaceId);
        dest.writeString(this.instanceId);
        dest.writeInt(this.urlScheme);
        dest.writeString(this.urlContent);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeString(this.uuid);
        dest.writeString(this.deviceName);
        dest.writeString(this.tagId);
    }

    public void readFromParcel(Parcel source) {
        this.frameType = source.readInt();
        this.advInterval = source.readInt();
        this.advDuration = source.readInt();
        this.rssi = source.readInt();
        this.txPower = source.readInt();
        this.namespaceId = source.readString();
        this.instanceId = source.readString();
        this.urlScheme = source.readInt();
        this.urlContent = source.readString();
        this.major = source.readInt();
        this.minor = source.readInt();
        this.uuid = source.readString();
        this.deviceName = source.readString();
        this.tagId = source.readString();
    }

    public TriggerStep2Bean() {
    }

    protected TriggerStep2Bean(Parcel in) {
        this.frameType = in.readInt();
        this.advInterval = in.readInt();
        this.advDuration = in.readInt();
        this.rssi = in.readInt();
        this.txPower = in.readInt();
        this.namespaceId = in.readString();
        this.instanceId = in.readString();
        this.urlScheme = in.readInt();
        this.urlContent = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.uuid = in.readString();
        this.deviceName = in.readString();
        this.tagId = in.readString();
    }

    public static final Parcelable.Creator<TriggerStep2Bean> CREATOR = new Parcelable.Creator<TriggerStep2Bean>() {
        @Override
        public TriggerStep2Bean createFromParcel(Parcel source) {
            return new TriggerStep2Bean(source);
        }

        @Override
        public TriggerStep2Bean[] newArray(int size) {
            return new TriggerStep2Bean[size];
        }
    };
}
