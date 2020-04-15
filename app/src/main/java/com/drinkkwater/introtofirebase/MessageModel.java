package com.drinkkwater.introtofirebase;

public class MessageModel {

    private String message,author,url;

    public MessageModel(){}

    public MessageModel(String  message, String author, String url){
        this.message = message;
        this.author = author;
        this.url = url;
    }

    /*
        Setter Functions
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    /*
        Getter Functions
     */

    public String getAuthor() {
        return author;
    }
    public String getMessage(){
        return message;
    }
    public String getUrl() { return url; }
}
