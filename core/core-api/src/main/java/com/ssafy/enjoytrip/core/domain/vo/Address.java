package com.ssafy.enjoytrip.core.domain.vo;

import java.io.Serializable;

public record Address(
        String address,
        String addressDetail,
        String zipcode
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public Address {
        address = address == null ? "" : address.trim();
        addressDetail = addressDetail == null ? "" : addressDetail.trim();
        zipcode = zipcode == null ? "" : zipcode.trim();
    }
}
