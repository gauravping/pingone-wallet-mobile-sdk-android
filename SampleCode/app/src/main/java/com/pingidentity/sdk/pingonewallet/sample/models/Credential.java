package com.pingidentity.sdk.pingonewallet.sample.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.pingidentity.did.sdk.types.Claim;

import java.io.IOException;

public class Credential implements Parcelable {

    private final Claim claim;
    private boolean isRevoked = false;

    public Credential(Claim claim) {
        this.claim = claim;
    }

    public Credential(Claim claim, boolean isRevoked) {
        this.claim = claim;
        this.isRevoked = isRevoked;
    }

    protected Credential(Parcel in) {
        isRevoked = in.readByte() != 0;
        try {
            claim = Claim.fromJson(in.readString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Claim getClaim() {
        return claim;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isRevoked ? 1 : 0));
        dest.writeString(claim.toJson());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Credential> CREATOR = new Creator<Credential>() {
        @Override
        public Credential createFromParcel(Parcel in) {
            return new Credential(in);
        }

        @Override
        public Credential[] newArray(int size) {
            return new Credential[size];
        }
    };

}
