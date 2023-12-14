package com.buddle.web3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class Web3jConfig {

    @Value("${config.infura-url}")
    private String INFURA_API_URL;

    @Value("${config.private-key}")
    private String PRIVATE_KEY;

    @Value("${config.contract-address}")
    private String CONTRACT_ADDRESS;


    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(INFURA_API_URL));
    }

    @Bean
    public Credentials credentials() {
        BigInteger privateKeyInBT = new BigInteger(PRIVATE_KEY, 16);
        return Credentials.create(ECKeyPair.create(privateKeyInBT));
    }

    @Bean
    public MyNFTs nft() {
        BigInteger gasPrice = new BigInteger("1000000000");
        BigInteger gasLimit = Contract.GAS_LIMIT;
        StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
        return MyNFTs.load(CONTRACT_ADDRESS, web3j(), credentials(), gasProvider);
    }
}