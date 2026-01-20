package com.example.event_indexer.listener;

import com.example.event_indexer.model.EventEntity;
import com.example.event_indexer.model.EventType;
import com.example.event_indexer.repo.EventRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final Web3j web3j;
    private final EventRepo eventRepo;

    @Value("${web3.contractAddress}")
    private String contractAddress;

    public static final Event DepositEvent = new Event(
            "Deposit",
            Arrays.asList(
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Uint256.class, false)
            )
    );

    public static final Event WithdrawEvent = new Event(
            "Withdraw",
            Arrays.asList(
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Uint256.class, false)
            )
    );

    public static final Event OwnershipTransferred = new Event(
            "OwnershipTransferred",
            Arrays.asList(
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class, true)
            )
    );

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        listenDeposit();
        listenWithdraw();
        listenOwnershipTransferred();
    }

    private void listenDeposit() {
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        );
        filter.addSingleTopic(EventEncoder.encode(DepositEvent));

        web3j.ethLogFlowable(filter).subscribe(
                eventLog -> {
                    List<Type> nonIndexed = FunctionReturnDecoder.decode(eventLog.getData(), DepositEvent.getNonIndexedParameters());

                    String from = (String) FunctionReturnDecoder.decodeIndexedValue(eventLog.getTopics().get(1), TypeReference.create(Address.class, true)).getValue();
                    BigInteger amount = (BigInteger) nonIndexed.getFirst().getValue();

                    EventEntity event = new EventEntity(EventType.DEPOSIT);
                    event.setFromAddress(from);
                    event.setAmount(amount);
                    eventRepo.save(event);
                    log.info("deposit event saved: from={}, amount={}", from, amount);
                },
                error -> {
                    log.error("error in deposit event listener: {}", error.getMessage(), error);
                    try {
                        Thread.sleep(5000);
                        listenDeposit();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
        );
    }

    private void listenWithdraw() {
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        );
        filter.addSingleTopic(EventEncoder.encode(WithdrawEvent));

        web3j.ethLogFlowable(filter).subscribe(
                eventLog -> {
                    List<Type> nonIndexed = FunctionReturnDecoder.decode(eventLog.getData(), WithdrawEvent.getNonIndexedParameters());

                    String to = (String) FunctionReturnDecoder.decodeIndexedValue(eventLog.getTopics().get(1), TypeReference.create(Address.class, true)).getValue();
                    BigInteger amount = (BigInteger) nonIndexed.getFirst().getValue();

                    EventEntity event = new EventEntity(EventType.WITHDRAW);
                    event.setToAddress(to);
                    event.setAmount(amount);
                    eventRepo.save(event);
                    log.info("withdraw event saved: to={}, amount={}", to, amount);
                },
                error -> {
                    log.error("error in withdraw event listener: {}", error.getMessage(), error);
                    try {
                        Thread.sleep(5000);
                        listenWithdraw();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
        );
    }

    private void listenOwnershipTransferred() {
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        );
        filter.addSingleTopic(EventEncoder.encode(OwnershipTransferred));

        web3j.ethLogFlowable(filter).subscribe(
                eventLog -> {
                    String from = (String) FunctionReturnDecoder.decodeIndexedValue(eventLog.getTopics().get(1), TypeReference.create(Address.class, true)).getValue();
                    String to = (String) FunctionReturnDecoder.decodeIndexedValue(eventLog.getTopics().get(2), TypeReference.create(Address.class, true)).getValue();

                    EventEntity event = new EventEntity(EventType.OWNERSHIPTRANSFERRED);
                    event.setPreviousOwner(from);
                    event.setNewOwner(to);
                    eventRepo.save(event);
                    log.info("ownershipTransferred event saved: from={}, to={}", from, to);
                },
                error -> {
                    log.error("error in ownershipTransferred event listener: {}", error.getMessage(), error);
                    try {
                        Thread.sleep(5000);
                        listenOwnershipTransferred();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
        );
    }
}
