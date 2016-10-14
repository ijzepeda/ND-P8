package com.ijzepeda.friendsknowsbest;

import java.util.Map;

/**
 * Created by Ivan on 10/14/2016.
 */

public class OnlineDeck {
    String category;
//    Map<String,Object> userVotes;//=new HAshMap<String,Object>(); users.put("users",""); root.updateChildren(users)
//    Map<String,Object> card;//>>CollectionCardNo[from Collection]
    Map<String,Map<String,Object>> card;
//TODO: CARD will be serialized in order to be shown, it will be made of CollectionCardNumber, and UserVotes
    public OnlineDeck() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public OnlineDeck(String category, Map<String, Map<String, Object>> card) {
        this.category = category;
        this.card = card;
    }

    public Map<String, Map<String, Object>> getCard() {
        return card;
    }

    public void setCard(Map<String, Map<String, Object>> card) {
        this.card = card;
    }
}
