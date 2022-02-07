package br.alu.ufc.robertcabral.consultorio.entity;

public class Fila {
    public String uid, nome;
    public int position;

    public Fila() {
    }

    public Fila(String uid, String nome, int position) {
        this.uid = uid;
        this.position = position;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }


}
