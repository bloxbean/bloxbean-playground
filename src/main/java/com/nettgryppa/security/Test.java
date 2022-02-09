package com.nettgryppa.security;

import java.security.NoSuchAlgorithmException;

public class Test {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        long timeNeeded = HashCash.estimateTime(40);
        System.out.println(timeNeeded);


    }
}
