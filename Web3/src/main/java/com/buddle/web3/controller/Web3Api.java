package com.buddle.web3.controller;

import com.buddle.web3.service.ContractService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

@ComponentScan
@Controller
public class Web3Api {
	@Value("https://magenta-persistent-gayal-887.mypinata.cloud/ipfs/QmR8zFJiKD1ZWWD3dip9mit52CEJzoWDN9hBUX9T7aiQGd?_gl=1*3olpes*_ga*ODYwMDcyMDI2LjE2OTkzMzYyNDM.*_ga_5RMPXG14TE*MTcwMDQ5Mzk4Ni4xMC4xLjE3MDA0OTQyMDMuMzMuMC4w")
    private String tokenURI;
	@Value("https://magenta-persistent-gayal-887.mypinata.cloud/ipfs/QmVhnaoUVH66hwk5MTNJCochm3oFn7D6avbs2YYCnR6frf?_gl=1*1u11dk4*_ga*ODYwMDcyMDI2LjE2OTkzMzYyNDM.*_ga_5RMPXG14TE*MTcwMDQ5Mzk4Ni4xMC4xLjE3MDA0OTQxOTkuMzcuMC4w")
	private String Coffee_URI;
	@Value("https://magenta-persistent-gayal-887.mypinata.cloud/ipfs/QmR9g3Mjbo2PKqGcPqLPmzvuQWwYrNaNk4qc1ZtEHtgKuK?_gl=1*djguyk*_ga*ODYwMDcyMDI2LjE2OTkzMzYyNDM.*_ga_5RMPXG14TE*MTcwMDQ5Mzk4Ni4xMC4xLjE3MDA0OTQyMzIuNC4wLjA.")
	private String Rest_URI;
	@Value("0x5a94340fb53bf7f945c1ee42c76be4117e60060f")
	private String FromAddress;
	@Value("0x721F63C3c0677C0FBfffA411646041F72f34efB1")
	private String ToAddress;
	private final ContractService contractService;
	public Web3Api(ContractService contractService) {
		this.contractService = contractService;
	}
	@GetMapping("/nft/balance")
	@ResponseBody
	public ResponseEntity<String> getEthBalance(){
		try {
			EthGetBalance ethGetBalance = contractService.getEthBalance();
			BigInteger balance = ethGetBalance.getBalance();
			return ResponseEntity.ok("balance : "+balance.toString());
		}catch(Exception e) {
			String errorMessage = "An error occurred: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
		}
	}

	@GetMapping("/nft")
	@ResponseBody
    public ResponseEntity<String> autoTx(@RequestParam String wallet,@RequestParam int n) {
		try {
			String URI = "NULL";
			switch (n){
				case 1: URI = tokenURI;
				        break;
				case 2: URI = Coffee_URI;
				        break;
				case 3: URI = Rest_URI;
				        break;
			}
			
			TransactionReceipt receiptMint = contractService.mintNFT(URI);
	        BigInteger Id = null; 
	        // Get the logs from the transaction receipt.
	        List<Log> logs = receiptMint.getLogs();

	        if (logs.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No logs found for the minting transaction.");
	        }
	        for (Log log : logs) {
	            String hexData = log.getData().substring(2);
	            if (!hexData.isEmpty() && !hexData.matches("^0*$")) {
	            	Id = new BigInteger(hexData, 16);
	            }
	            else {
	            	Id = new BigInteger("404");
	            }
	        }
			TransactionReceipt receiptTransfer = contractService.safeTransferFrom(FromAddress,wallet,Id);
			return ResponseEntity.ok(""+"Minting successfully. Mint hash : " + receiptMint.getTransactionHash() + "Transfer successfully. Transaction hash: " + receiptTransfer.getTransactionHash() +" To : " + wallet + " Token id : "+Id);
		}catch(Exception e) {
			e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error minting NFT: " + e.getMessage() + wallet + " " +n);
		}
    }
	
}


