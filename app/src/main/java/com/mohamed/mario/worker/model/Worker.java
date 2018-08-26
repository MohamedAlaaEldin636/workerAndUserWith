package com.mohamed.mario.worker.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Worker {

    private String name;
    private String password;
    private String phone;
    private String location;

    ////////Worker
    //TODO Resize Photos
    private String personalImage;
    private String profession;
    private String description;
    /**
     * Max 5 images
     */
    private ArrayList<String> workImages;
    private Review review;
    private float rate;
    // counter
    private int ViewedBy;

    public Worker(String name, String password, String phone, String location, String personalImage, String profession, String description, ArrayList<String> workImages, Review review, float rate, int viewedBy) {
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.location = location;
        this.personalImage = personalImage;
        this.profession = profession;
        this.description = description;
        this.workImages = workImages;
        this.review = review;
        this.rate = rate;
        ViewedBy = viewedBy;
    }

    public Worker() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPersonalImage() {
        return personalImage;
    }

    public void setPersonalImage(String personalImage) {
        this.personalImage = personalImage;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getWorkImages() {
        return workImages;
    }

    public void setWorkImages(ArrayList<String> workImages) {
        this.workImages = workImages;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getViewedBy() {
        return ViewedBy;
    }

    public void setViewedBy(int viewedBy) {
        ViewedBy = viewedBy;
    }
}

