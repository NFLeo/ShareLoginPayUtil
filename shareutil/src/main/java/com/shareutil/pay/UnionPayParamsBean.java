/*
******************************* Copyright (c)*********************************\
**
**                 (c) Copyright 2017, King, china
**                          All Rights Reserved
**                                
**                      By(King)
**                         
**------------------------------------------------------------------------------
*/
package com.shareutil.pay;

import android.os.Parcel;
import android.os.Parcelable;

public class UnionPayParamsBean implements IPayParamsBean, Parcelable {

    private String mode;
    private String tn;

    public UnionPayParamsBean() {  }

    private UnionPayParamsBean(Parcel in) {
        tn = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tn);
    }

    public static final Creator<UnionPayParamsBean> CREATOR = new Creator<UnionPayParamsBean>() {
        @Override
        public UnionPayParamsBean createFromParcel(Parcel in) {
            return new UnionPayParamsBean(in);
        }

        @Override
        public UnionPayParamsBean[] newArray(int size) {
            return new UnionPayParamsBean[size];
        }
    };

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
