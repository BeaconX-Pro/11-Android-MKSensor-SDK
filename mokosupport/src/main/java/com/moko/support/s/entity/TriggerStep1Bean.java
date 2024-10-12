package com.moko.support.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: jun.liu
 * @date: 2024/10/9 17:24
 * @des:
 */
public class TriggerStep1Bean implements Parcelable {
    public int triggerType;
    public int triggerCondition;
    public int axisStaticPeriod;
    public int tempThreshold;
    public int humThreshold;
    public int lockedAdvDuration;
    public int slot;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.triggerType);
        dest.writeInt(this.triggerCondition);
        dest.writeInt(this.axisStaticPeriod);
        dest.writeInt(this.tempThreshold);
        dest.writeInt(this.humThreshold);
        dest.writeInt(this.lockedAdvDuration);
        dest.writeInt(this.slot);
    }

    public void readFromParcel(Parcel source) {
        this.triggerType = source.readInt();
        this.triggerCondition = source.readInt();
        this.axisStaticPeriod = source.readInt();
        this.tempThreshold = source.readInt();
        this.humThreshold = source.readInt();
        this.lockedAdvDuration = source.readInt();
        this.slot = source.readInt();
    }

    public TriggerStep1Bean() {
    }

    protected TriggerStep1Bean(Parcel in) {
        this.triggerType = in.readInt();
        this.triggerCondition = in.readInt();
        this.axisStaticPeriod = in.readInt();
        this.tempThreshold = in.readInt();
        this.humThreshold = in.readInt();
        this.lockedAdvDuration = in.readInt();
        this.slot = in.readInt();
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
