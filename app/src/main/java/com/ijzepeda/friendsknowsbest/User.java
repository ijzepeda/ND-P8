package com.ijzepeda.friendsknowsbest;


//import static com.ijzepeda.tatt.R.id.nameTV;

/**
 * Created by Ivan on 10/3/2016.
 */

public class User {
    String name, lastName, phone, address, card, billingAddress,uid,email;



    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", card='" + card + '\'' +
                ", billingAddress='" + billingAddress + '\'' +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public User(String name, String lastName, String phone, String address, String card, String billingAddress, String uid, String email) {
        this.name = name;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.card = card;
        this.billingAddress = billingAddress;
        this.uid = uid;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameTV) {
        this.name = nameTV;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
