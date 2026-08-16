package com.example.unyieldingmight;

import java.util.Date;

public class Profile {
    private final String email;
    private final String password;
    private final int age;
    private final Address address;
    private final Date dateOfBirth;
    private final Gender gender;
    private final String firstName;
    private final String lastName;

    private Profile(Builder builder) {
        this.email = builder.email;
        this.password = builder.password;
        this.age = builder.age;
        this.address = builder.address;
        this.dateOfBirth = builder.dateOfBirth;
        this.gender = builder.gender;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() { return age; }

    public Address getAddress() {
        return address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static class Builder {
        private String email;
        private String password;
        private int age;
        private Address address;
        private Date dateOfBirth;
        private Gender gender;
        private String firstName;
        private String lastName;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder address(Address address) {
            this.address = address;
            return this;
        }

        public Builder dateOfBirth(Date dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(Gender male) {
            this.gender = male;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Profile build() { return new Profile(this); }
    }

}