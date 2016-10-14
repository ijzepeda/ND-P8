package com.ijzepeda.friendsknowsbest;

import java.util.Map;

/**
 * Created by Ivan on 10/14/2016.
 */

public class DeckCard {
    int collectionCardNo;
    Map<String,Object>users;

    public DeckCard() {
    }

    public DeckCard(int collectionCardNo, Map<String, Object> users) {
        this.collectionCardNo = collectionCardNo;
        this.users = users;
    }

    public int getCollectionCardNo() {
        return collectionCardNo;
    }

    public void setCollectionCardNo(int collectionCardNo) {
        this.collectionCardNo = collectionCardNo;
    }

    public Map<String, Object> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Object> users) {
        this.users = users;
    }
}
