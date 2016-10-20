package com.ijzepeda.friendsknowsbest;


//import static com.ijzepeda.tatt.R.id.nameTV;

import java.util.Map;

/**
 * Created by Ivan on 10/3/2016.
 */

public class User {
    String name, age,earnedCards,email,uid, photoUrl;
    Map<String,Object> friends;//=new HAshMap<String,Object>(); users.put("users",""); root.updateChildren(users)
    Map<String,Object> cards;
    Map<String,Object> games;

      public User() {
    }

    public User(String name, String age, String earnedCards, String email, String uid, String photoUrl, Map<String, Object> friends, Map<String, Object> cards, Map<String, Object> games) {
        this.name = name;
        this.age = age;
        this.earnedCards = earnedCards;
        this.email = email;
        this.uid = uid;
        this.photoUrl = photoUrl;
        this.friends = friends;
        this.cards = cards;
        this.games = games;
    }

    public User(String name, String uid, String photoUrl) {
        this.name = name;
        this.uid = uid;
        this.photoUrl = photoUrl;
    }
//    public User(String name, String age, String earnedCards, String email, String uid, Map<String, Object> friends, Map<String, Object> cards, Map<String, Object> games) {
//        this.name = name;
//        this.age = age;
//        this.earnedCards = earnedCards;
//        this.email = email;
//        this.uid = uid;
//        this.friends = friends;
//        this.cards = cards;
//        this.games = games;
//    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getEarnedCards() {
        return earnedCards;
    }

    public void setEarnedCards(String earnedCards) {
        this.earnedCards = earnedCards;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Object> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Object> friends) {
        this.friends = friends;
    }

    public Map<String, Object> getCards() {
        return cards;
    }

    public void setCards(Map<String, Object> cards) {
        this.cards = cards;
    }

    public Map<String, Object> getGames() {
        return games;
    }

    public void setGames(Map<String, Object> games) {
        this.games = games;
    }
}
