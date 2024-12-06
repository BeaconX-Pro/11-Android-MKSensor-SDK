package com.moko.support.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class SlotData implements Parcelable {
    // iBeacon
    public String uuid;
    public int major;
    public int minor;
    // URL
    public int urlScheme = 1;
    public String urlContent = "mokobule.com/";
    // UID
    public String namespace;
    public String instanceId;
    // TLM
    // No data
    // sensor info
    public String deviceName;
    public String tagId;

    // BaseParam
    public int rssi;
    public int txPower;
    public int advInterval = 1000;
    public int advDuration = 10;
    public int standbyDuration;
    public int slot;
    public int currentFrameType;
    public int step1TriggerType;
    public int realType;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.urlScheme);
        dest.writeString(this.urlContent);
        dest.writeString(this.namespace);
        dest.writeString(this.instanceId);
        dest.writeString(this.deviceName);
        dest.writeString(this.tagId);
        dest.writeInt(this.rssi);
        dest.writeInt(this.txPower);
        dest.writeInt(this.advInterval);
        dest.writeInt(this.advDuration);
        dest.writeInt(this.standbyDuration);
        dest.writeInt(this.slot);
        dest.writeInt(this.currentFrameType);
        dest.writeInt(this.step1TriggerType);
        dest.writeInt(this.realType);
    }

    public void readFromParcel(Parcel source) {
        this.uuid = source.readString();
        this.major = source.readInt();
        this.minor = source.readInt();
        this.urlScheme = source.readInt();
        this.urlContent = source.readString();
        this.namespace = source.readString();
        this.instanceId = source.readString();
        this.deviceName = source.readString();
        this.tagId = source.readString();
        this.rssi = source.readInt();
        this.txPower = source.readInt();
        this.advInterval = source.readInt();
        this.advDuration = source.readInt();
        this.standbyDuration = source.readInt();
        this.slot = source.readInt();
        this.currentFrameType = source.readInt();
        this.step1TriggerType = source.readInt();
        this.realType = source.readInt();
    }

    public SlotData() {
    }

    protected SlotData(Parcel in) {
        this.uuid = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.urlScheme = in.readInt();
        this.urlContent = in.readString();
        this.namespace = in.readString();
        this.instanceId = in.readString();
        this.deviceName = in.readString();
        this.tagId = in.readString();
        this.rssi = in.readInt();
        this.txPower = in.readInt();
        this.advInterval = in.readInt();
        this.advDuration = in.readInt();
        this.standbyDuration = in.readInt();
        this.slot = in.readInt();
        this.currentFrameType = in.readInt();
        this.step1TriggerType = in.readInt();
        this.realType = in.readInt();
    }

    public static final Parcelable.Creator<SlotData> CREATOR = new Parcelable.Creator<SlotData>() {
        @Override
        public SlotData createFromParcel(Parcel source) {
            return new SlotData(source);
        }

        @Override
        public SlotData[] newArray(int size) {
            return new SlotData[size];
        }
    };
}
