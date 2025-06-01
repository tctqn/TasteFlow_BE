package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.UserVoucher;

public interface UserVoucherService {

    UserVoucher claimVoucher(Long userId, Long voucherId);

    UserVoucher useVoucher(Long userVoucherId);

}