package com.example.contactcopy.Entity;

import android.os.Parcel;
import android.os.Parcelable;

public class person implements Parcelable {
    private String contract;
    private String number;

    public person(Parcel in) {
        contract = in.readString();
        number = in.readString();
    }

    public person(String contract, String number) {
        this.contract = contract;
        this.number = number;
    }

    public static final Creator<person> CREATOR = new Creator<person>() {
        @Override
        public person createFromParcel(Parcel in) {
            return new person(in);
        }

        @Override
        public person[] newArray(int size) {
            return new person[size];
        }
    };

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contract);
        dest.writeString(number);
    }
}
