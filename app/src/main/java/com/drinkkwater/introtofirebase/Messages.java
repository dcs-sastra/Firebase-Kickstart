package com.drinkkwater.introtofirebase;

public class Messages {
    private String message,author,url;
    public Messages(){}
    public Messages(String  message,String author,String url){
        this.message = message;
        this.author = author;
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }
    public String getMessage(){
        return message;
    }
    public String getUrl() { return url; }
}
