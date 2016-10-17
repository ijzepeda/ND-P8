package com.ijzepeda.friendsknowsbest;

/**
 * Created by Ivan on 10/14/2016.
 */

public class UserVote {
    String name;
    String useruid;

    String picUrl;
    String message="";
    boolean voted;
    String nomineeUID;
    String nomineeName;
    String nomineePicUrl;
    /**When user goes to Result Activity, if they accept the results they turn this on, then if everyone has accepted, the game continues*/
    boolean acceptResult;

    public UserVote() {
    }

    public String getUseruid() {
        return useruid;
    }

    public void setUseruid(String useruid) {
        this.useruid = useruid;
    }

    public String getNomineePicUrl() {
        return nomineePicUrl;
    }

    public void setNomineePicUrl(String nomineePicUrl) {
        this.nomineePicUrl = nomineePicUrl;
    }

    public boolean isAcceptResult() {
        return acceptResult;
    }

    public void setAcceptResult(boolean acceptResult) {
        this.acceptResult = acceptResult;
    }

    public UserVote(String name, String useruid, String picUrl, String message, boolean voted, String nomineeUID, String nomineeName, String nomineePicUrl, boolean acceptResult) {
        this.name = name;
        this.useruid = useruid;
        this.picUrl = picUrl;
        this.message = message;
        this.voted = voted;
        this.nomineeUID = nomineeUID;
        this.nomineeName = nomineeName;
        this.nomineePicUrl = nomineePicUrl;
        this.acceptResult = acceptResult;
    }
//    public UserVote(String name, String useruid, String picUrl, String message, boolean voted, String nomineeUID, String nomineeName) {
//        this.name = name;
//        this.useruid = useruid;
//        this.picUrl = picUrl;
//        this.message = message;
//        this.voted = voted;
//        this.nomineeUID = nomineeUID;
//        this.nomineeName = nomineeName;
//    }

//    public UserVote(String name, String picUrl, String message, boolean voted, String nomineeUID, String nomineeName) {
//        this.name = name;
//        this.picUrl = picUrl;
//        this.message = message;
//        this.voted = voted;
//        this.nomineeUID = nomineeUID;
//        this.nomineeName = nomineeName;
//    }

//    public UserVote(String name, String message, boolean voted, String nomineeUID, String nomineeName) {
//        this.name = name;
//        this.message = message;
//        this.voted = voted;
//        this.nomineeUID = nomineeUID;
//        this.nomineeName = nomineeName;
//    }

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
