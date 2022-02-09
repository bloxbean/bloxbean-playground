package com.bloxbean.playground.minter;

import com.bloxbean.cardano.client.cip.cip25.NFT;
import com.bloxbean.cardano.client.cip.cip25.NFTFile;
import jakarta.inject.Singleton;

import java.util.Random;

@Singleton
public class NFTProvider {

    public NFT getRandomNft() {
        String name = generateName(10);
        String imgData = "ipfs://Qmcv6hwtmdVumrNeb42R1KmCEWdYWGcqNgs17Y3hj6CkP4";
        NFT nft = NFT.create()
                .assetName(name)
                .name(name)
                .image("ipfs://Qmcv6hwtmdVumrNeb42R1KmCEWdYWGcqNgs17Y3hj6CkP4")
                .mediaType("image/png")
                .addFile(NFTFile.create()
                        //.name("file-1")
                        .mediaType("image/png")
                        .src(imgData))
                .description("This is a random generated image")
                .property("Creator", "Multi-sig minter")
                .property("Generated By", "cardano-client-lib");

        return nft;
    }

    private  String generateName(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                +"lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}