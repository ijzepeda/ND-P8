package com.ijzepeda.friendsknowsbest;

/**
 * Created by Ivan on 10/14/2016.
 */

public class UserVote {
    String name;
    String picUrl;
    String message;
    boolean voted;
    String nomineeUID;
    String nomineeName;

    public UserVote() {
    }

    public UserVote(String name, String picUrl, String message, boolean voted, String nomineeUID, String nomineeName) {
        this.name = name;
        this.picUrl = picUrl;
        this.message = message;
        this.voted = voted;
        this.nomineeUID = nomineeUID;
        this.nomineeName = nomineeName;
    }

    public UserVote(String name, String message, boolean voted, String nomineeUID, String nomineeName) {
        this.name = name;
        this.message = message;
        this.voted = voted;
        this.nomineeUID = nomineeUID;
        this.nomineeName = nomineeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public String getNomineeUID() {
        return nomineeUID;
    }

    public void setNomineeUID(String nomineeUID) {
        this.nomineeUID = nomineeUID;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    @Override
    public String toString() {
        return "UserVote{" +
                "name='" + name + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", message='" + message + '\'' +
                ", voted=" + voted +
                ", nomineeUID='" + nomineeUID + '\'' +
                ", nomineeName='" + nomineeName + '\'' +
                '}';
    }
}
