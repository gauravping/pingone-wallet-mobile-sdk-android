package com.pingidentity.sdk.pingonewallet.sample.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.pingidentity.sdk.pingonewallet.sample.utils.BitmapUtil;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Profile {

    private String firstName;
    private String lastName;
    private String email;
    private Bitmap selfie;

    public Profile(Map<String, String> map) {
        this.firstName = map.getOrDefault("firstName", "");
        this.lastName = map.getOrDefault("lastName", "");
        this.email = map.getOrDefault("email", "");
        byte[] decodedImage = Base64.decode(map.getOrDefault("selfie", ""), Base64.DEFAULT);
        this.selfie = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
    }

    public Profile(String firstName, String lastName, String email, Bitmap selfie) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.selfie = selfie;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getSelfie() {
        return selfie;
    }

    public void setSelfie(Bitmap selfie) {
        this.selfie = selfie;
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("email", email);
        map.put("selfie", BitmapUtil.bitmapToBase64(selfie));
        return map;
    }

}
