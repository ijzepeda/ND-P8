package com.ijzepeda.friendsknowsbest.models;

/**
 * Created by ivan.zepeda on 12/07/2016.
 */

public class Card {

    int id;
    int number;
    String quote;
    String category;
    boolean favorite;

    public Card(int id, int number,String quote, String category,  boolean favorite) {
        this.category = category;
        this.quote = quote;
        this.id = id;
        this.number = number;
        this.favorite = favorite;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
