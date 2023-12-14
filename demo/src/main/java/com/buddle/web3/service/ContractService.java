package com.buddle.web3.service;

import com.buddle.web3.config.MyNFTs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@Service("ContractService")
public class ContractService {

    private final Web3j web3j;
    private final MyNFTs nft;

    @Value("${config.wallet-address}")
    private String WALLET_ADDRESS;

    @Value("${config.contract-address}")
    private String CONTRACT_ADDRESS;


    @Autowired
    public ContractService(Web3j web3j, MyNFTs nft) {
        this.web3j = web3j;
        this.nft = nft;
    }

    // 현재 블록 번호
    public EthBlockNumber getBlockNumber() throws ExecutionException, InterruptedException {
        return web3j.ethBlockNumber().sendAsync().get();
    }

    // 지정된 주소의 계정
    public EthAccounts getEthAccounts() throws ExecutionException, InterruptedException {
        return web3j.ethAccounts().sendAsync().get();
    }

    // 계좌 거래 건수
    public EthGetTransactionCount getTransactionCount() throws ExecutionException, InterruptedException {
        EthGetTransactionCount result = new EthGetTransactionCount();
        return web3j.ethGetTransactionCount(WALLET_ADDRESS,
                        DefaultBlockParameter.valueOf("latest"))
                .sendAsync()
                .get();
    }

    // 계정 잔액 조회
    public EthGetBalance getEthBalance() throws ExecutionException, InterruptedException {
        return web3j.ethGetBalance(WALLET_ADDRESS,
                        DefaultBlockParameter.valueOf("latest"))
                .sendAsync()
                .get();
    }

    // 스마트컨트랙트명 가져오기
    public String getContractName() throws Exception {
        return nft.name().send();
    }
    public class MintNFTResponse{
    	private boolean success;
    	private String message;
		public void setSuccess(boolean b) {
			success = b;
		}
		public void setMessage(String string) {
			message = string;
		}
    }
    // nft 발행 건수
    public BigInteger currentCount() throws Exception {
        return nft.balanceOf(WALLET_ADDRESS).send();
    }

    // nft 발행
    
    public TransactionReceipt mintNFT(String tokenURI) throws Exception {
        return nft.mintNFT(tokenURI).send();
    }
    public String getContractSymbol() throws Exception {
        RemoteCall<String> symbolCall = nft.symbol();
        return symbolCall.sendAsync().get();
    }

    public TransactionReceipt safeTransferFrom(String from, String to, BigInteger tokenId) {
    	try {
			return nft.safeTransferFrom(from, to, tokenId).send();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    // 이더리움 블록체인에서 발생하는 이벤트를 필터링하는데 사용(여기에서는 Transfer(거래)만 허용)
    private EthFilter getEthFilter() throws Exception {
        EthBlockNumber blockNumber = getBlockNumber();
        EthFilter ethFilter = new EthFilter(DefaultBlockParameter.valueOf(blockNumber.getBlockNumber()), DefaultBlockParameterName.LATEST, CONTRACT_ADDRESS);

        Event event = new Event("Transfer",
                Arrays.asList(
                        new TypeReference<Address>(true) {
                            // from
                        },
                        new TypeReference<Address>(true) {
                        },
                        new TypeReference<Uint256>(false) {
                            // amount
                        }
                ));
        String topicData = EventEncoder.encode(event);
        ethFilter.addSingleTopic(topicData);
        ethFilter.addNullTopic();// filter: event type (topic[0])
        //ethFilter.addOptionalTopics("0x"+ TypeEncoder.encode(new Address("")));

        return ethFilter;
    }


}
