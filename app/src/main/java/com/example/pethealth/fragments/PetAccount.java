package com.example.pethealth.fragments;

public class PetAccount {
    private String image;
    private String name;
    private String birthday;
    private String species;
    private String gender;
    private String weight;
    private String uid;

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public String getName() { return name; }

    public  void setName(String name) { this.name = name; }

    public String getBirthday() { return birthday; }

    public  void setBirthday(String birthday) { this.birthday = birthday; }

    public  String getSpecies() { return species; }

    public  void setSpecies(String species) { this.species = species; }

    public String getGender() { return gender; }

    public  void setGender(String gender) { this.gender = gender; }

}
