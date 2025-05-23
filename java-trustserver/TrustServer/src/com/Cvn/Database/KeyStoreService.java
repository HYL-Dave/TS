package com.Cvn.Database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeyStoreService {

    private Logger logger = LogManager.getLogger(KeyStoreService.class);

    @Autowired
    KeyStoreRepo keyStoreRepo;

    public List<KeyStore> list() {
        return keyStoreRepo.findAll();
    }

    public KeyStore findOne(String alias) {
        String[] key = null;
        KeyStore keyStore = keyStoreRepo.findByAlias(alias);
        return keyStore;
    }

    public String storeKey(String alias, String privKey, String pubKey) {
        KeyStore keyStore = new KeyStore();
        keyStore.setAlias(alias);
        keyStore.setPrikey(privKey);
        keyStore.setPubkey(pubKey);
        logger.info("KeyStore to be store: {}",keyStore.toString());
        keyStoreRepo.saveAndFlush(keyStore);
        return "0";
    }

}
