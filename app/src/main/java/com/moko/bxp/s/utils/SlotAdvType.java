package com.moko.bxp.s.utils;

/**
 * @author: jun.liu
 * @date: 2024/1/18 18:29
 * @des:
 */
public class SlotAdvType {
    public static String getSlotAdvType(int type){
        switch (type){
            case 0x00:
                return "UID";
            case 0x10:
                return "URL";
            case 0x20:
                return "TLM";
            case 0x50:
                return "iBeacon";
            case 0x70:
                return "T&H_INFOR";
            case 0x80:
                return "Tag";
            case 0xFF:
                return "No data";
        }
        return "No data";
    }
}
