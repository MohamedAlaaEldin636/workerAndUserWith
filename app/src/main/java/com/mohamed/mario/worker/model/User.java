package com.mohamed.mario.worker.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {

    public String name;
    public String phone;
    public String password;
    public String personalImage;
    public ArrayList<Worker> workersRated;
    public ArrayList<Worker> workersReviewed;
    public ArrayList<Worker> lastFiveWorkersVisitedByThisUser;
    // todo this is for geoFire
    public String location;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String phone, String password, String personalImage, ArrayList<Worker> workersRated, ArrayList<Worker> workersReviewed, ArrayList<Worker> lastFiveWorkersVisitedByThisUser, String location) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.personalImage = personalImage;
        this.workersRated = workersRated;
        this.workersReviewed = workersReviewed;
        this.lastFiveWorkersVisitedByThisUser = lastFiveWorkersVisitedByThisUser;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPersonalImage() {
        return personalImage;
    }

    public void setPersonalImage(String personalImage) {
        this.personalImage = personalImage;
    }

    public ArrayList<Worker> getWorkersRated() {
        return workersRated;
    }

    public void setWorkersRated(ArrayList<Worker> workersRated) {
        this.workersRated = workersRated;
    }

    public ArrayList<Worker> getWorkersReviewed() {
        return workersReviewed;
    }

    public void setWorkersReviewed(ArrayList<Worker> workersReviewed) {
        this.workersReviewed = workersReviewed;
    }

    public ArrayList<Worker> getLastFiveWorkersVisitedByThisUser() {
        return lastFiveWorkersVisitedByThisUser;
    }

    public void setLastFiveWorkersVisitedByThisUser(ArrayList<Worker> lastFiveWorkersVisitedByThisUser) {
        this.lastFiveWorkersVisitedByThisUser = lastFiveWorkersVisitedByThisUser;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
