package com.moko.bxp.s.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author: jun.liu
 * @date: 2024/9/25 16:42
 * @des:
 */
public class TriggerEvent implements Parcelable {
    public int triggerType;
    public int triggerCondition;
    public int triggerThreshold;
    public int lockAdvDuration;
    public int staticPeriod;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.triggerType);
        dest.writeInt(this.triggerCondition);
        dest.writeInt(this.triggerThreshold);
        dest.writeInt(this.lockAdvDuration);
        dest.writeInt(this.staticPeriod);
    }

    public void readFromParcel(Parcel source) {
        this.triggerType = source.readInt();
        this.triggerCondition = source.readInt();
        this.triggerThreshold = source.readInt();
        this.lockAdvDuration = source.readInt();
        this.staticPeriod = source.readInt();
    }

    public TriggerEvent() {
    }

    protected TriggerEvent(Parcel in) {
        this.triggerType = in.readInt();
        this.triggerCondition = in.readInt();
        this.triggerThreshold = in.readInt();
        this.lockAdvDuration = in.readInt();
        this.staticPeriod = in.readInt();
    }

    public static final Parcelable.Creator<TriggerEvent> CREATOR = new Parcelable.Creator<TriggerEvent>() {
        @Override
        public TriggerEvent createFromParcel(Parcel source) {
            return new TriggerEvent(source);
        }

        @Override
        public TriggerEvent[] newArray(int size) {
            return new TriggerEvent[size];
        }
    };
}
