package com.mohamed.mario.worker.model;

import java.util.ArrayList;

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

}
