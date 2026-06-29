package com.example.nasa_apod;

public class ApodResponse {
    private String date;
    private String explanation;
    private String hdurl;
    private String media_type;
    private String title;
    private String url;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getHdurl() { return hdurl; }
    public void setHdurl(String hdurl) { this.hdurl = hdurl; }

    public String getMedia_type() { return media_type; }
    public void setMedia_type(String media_type) { this.media_type = media_type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
