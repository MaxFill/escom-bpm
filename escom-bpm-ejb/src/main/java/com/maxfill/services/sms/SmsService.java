package com.maxfill.services.sms;

import com.maxfill.model.basedict.user.User;

public interface SmsService{
    String generatePinCode();
    String sendAccessCode(String phone, String pinCode);
    boolean isActive();
}
