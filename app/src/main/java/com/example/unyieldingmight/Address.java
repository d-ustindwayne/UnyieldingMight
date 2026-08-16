package com.example.unyieldingmight;

public class Address {

    private final String street;
    private final String city;
    private final String region;
    private final String postcode;
    private final String country;

    public Address(Builder builder) {
        this.street = builder.street;
        this.city = builder.city;
        this.region = builder.region;
        this.postcode = builder.postcode;
        this.country = builder.country;
    }

    @Override
    public String toString() {
        return street + ", " + city + ", " + region + " " + postcode + " " + country;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }

    public static class Builder {
        private String street;
        private String city;
        private String region;
        private String postcode;
        private String country;

        public Builder street(String street) {
            this.street = street;
            return this;
        }
        public Builder city(String city) {
            this.city = city;
            return this;
        }
        public Builder region(String region) {
            this.region = region;
            return this;
        }
        public Builder postcode(String postcode) {
            this.postcode = postcode;
            return this;
        }
        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Address build() { return new Address(this); }
    }
}
