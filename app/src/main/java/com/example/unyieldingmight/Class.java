package com.example.unyieldingmight;
// Variables used to hold data which is going to be displayed in recycler_item.xml and activity_detail.xml
public class Class {
    private String title;
    private String instructor;
    private String intensity;
//  R.string are integers
    private int description;
    private String date;
    private String startTime;
    private String endTime;
    private String curCap;
    private String maxCap;
//  @drawable are integers
    private int image;
//  Constructor

    public Class(String title, String instructor, String intensity, int description, String date, String startTime, String endTime, String curCap, String maxCap, int image) {
        this.image = image;
        this.title = title;
        this.instructor = instructor;
        this.intensity = intensity;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.curCap = curCap;
        this.maxCap = maxCap;
        this.description = description;

    }
    public String getTitle() {
        return title;
    }
    public String getInstructor() {
        return instructor;
    }
    public String getIntensity() {
        return intensity;
    }
    public int getDescription() {
        return description;
    }
    public String getDate() {
        return date;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public String getCurCap() {
        return curCap;
    }
    public String getMaxCap() {
        return maxCap;
    }
    public int getImage() {
        return image;
    }

}
