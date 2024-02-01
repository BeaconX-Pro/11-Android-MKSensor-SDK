package com.moko.bxp.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: jun.liu
 * @date: 2024/2/1 10:24
 * @des:
 */
public class TriggerStep2Bean implements Parcelable {
    public int triggerType;
    public int triggerEventSelect;
    public int axisStaticPeriod;
    public int tempThreshold;
    public int humThreshold;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.triggerType);
        dest.writeInt(this.triggerEventSelect);
        dest.writeInt(this.axisStaticPeriod);
        dest.writeInt(this.tempThreshold);
        dest.writeInt(this.humThreshold);
    }

    public void readFromParcel(Parcel source) {
        this.triggerType = source.readInt();
        this.triggerEventSelect = source.readInt();
        this.axisStaticPeriod = source.readInt();
        this.tempThreshold = source.readInt();
        this.humThreshold = source.readInt();
    }

    public TriggerStep2Bean() {
    }

    protected TriggerStep2Bean(Parcel in) {
        this.triggerType = in.readInt();
        this.triggerEventSelect = in.readInt();
        this.axisStaticPeriod = in.readInt();
        this.tempThreshold = in.readInt();
        this.humThreshold = in.readInt();
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
