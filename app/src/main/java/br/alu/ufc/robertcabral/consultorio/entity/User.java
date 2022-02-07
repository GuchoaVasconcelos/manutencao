package br.alu.ufc.robertcabral.consultorio.entity;

public class User {
    String uid, name, email, dateBirth;

    public User(){

    }

    public User(String uid, String name, String email, String dateBirth) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.dateBirth = dateBirth;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDateBirth(String dateBirth) {
        this.dateBirth = dateBirth;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDateBirth() {
        return dateBirth;
    }
}
