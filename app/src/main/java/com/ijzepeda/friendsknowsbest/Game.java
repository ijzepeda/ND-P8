package com.ijzepeda.friendsknowsbest;

import java.util.Map;

/**
 * Created by Ivan on 10/13/2016.
 */

public class Game {
    int currentCard;
    String deckId;
    String name;
    int noUsers;
    int noCards;

    String uid;
    boolean unlimitedCounter;
    Map<String,Object> users;//=new HAshMap<String,Object>(); users.put("users",""); root.updateChildren(users)

    public Game() {
    }

    public Game(int currentCard, String deckId, String name, int noUsers,int noCards, String uid, boolean unlimitedCounter) {
        this.currentCard = currentCard;
        this.deckId = deckId;
        this.name = name;
        this.noUsers = noUsers;
        this.noCards = noCards;
        this.uid = uid;
        this.unlimitedCounter = unlimitedCounter;
    }

    public int getNoCards() {
        return noCards;
    }

    public void setNoCards(int noCards) {
        this.noCards = noCards;
    }

    public Game(int currentCard, String deckId, String name, int noUsers,int noCards, String uid, boolean unlimitedCounter, Map<String, Object> map) {
        this.currentCard = currentCard;
        this.deckId = deckId;
        this.name = name;
        this.noUsers = noUsers;
        this.noCards = noCards;
        this.uid = uid;
        this.unlimitedCounter = unlimitedCounter;
        this.users = map;
    }

    public int getCurrentCard() {
        return currentCard;
    }

    public void setCurrentCard(int currentCard) {
        this.currentCard = currentCard;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNoUsers() {
        return noUsers;
    }

    public void setNoUsers(int noUsers) {
        this.noUsers = noUsers;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isUnlimitedCounter() {
        return unlimitedCounter;
    }

    public void setUnlimitedCounter(boolean unlimitedCounter) {
        this.unlimitedCounter = unlimitedCounter;
    }

    public Map<String, Object> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Object> users) {
        this.users = users;
    }
}
