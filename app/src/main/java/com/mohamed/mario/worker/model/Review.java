package com.mohamed.mario.worker.model;

public class Review {

    private String name;

    private String comment;

    // todo
    private String image;

    public Review(String name, String comment, String image) {
        this.name = name;
        this.comment = comment;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
