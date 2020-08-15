package com.example.assetmanager;

public class userId {

    private static userId instance;

    private String data;

    private userId(){}

    public void setId(String s){
        this.data=s;
    }
    public String getId(){
        return this.data;
    }

    public static synchronized userId getInstance(){
        if(instance==null){
            instance=new userId();
        }
        return instance;
    }

}
