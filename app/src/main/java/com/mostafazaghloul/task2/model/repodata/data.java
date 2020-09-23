package com.mostafazaghloul.task2.model.repodata;

public class data {
    String title,owner,description,html_url;
    boolean fork;

    public boolean isFork() {
        return fork;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }
    public data(String title,String owner,String description,Boolean fork,String html_url){
        this.title = title;
        this.description = description;
        this.owner = owner;
        this.fork=fork;
        this.html_url = html_url;
    }

    public String getHtml_url() {
        return html_url;
    }
}
