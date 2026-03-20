package com.ds.devsuaccount.application;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    public boolean isValidAccount(String clientId, String account) {
        return true;
    }

    public boolean haveEnoughAmount(String clientId, String account) {
        return true;
    }

    public boolean debitAmount(String clientId, String account, double amount) {
        return true;
    }

    public boolean creditAmount(String clientId, String account, double amount) {
        return true;
    }
}
