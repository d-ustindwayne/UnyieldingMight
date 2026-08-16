package com.example.unyieldingmight;

public class Admin {
    private final int adminId;

    Admin(Builder builder){
        this.adminId = builder.adminId;
    }

    public int getAdminId(){
        return adminId;
    }

    //Builder Class
    public static class Builder{
        private int adminId;

        public Builder adminId (int adminId){
            this.adminId = adminId;
            return this;
        }
    }

    /** public manageCustomer(){

    }

    public manageClass(){

    } **/
}
