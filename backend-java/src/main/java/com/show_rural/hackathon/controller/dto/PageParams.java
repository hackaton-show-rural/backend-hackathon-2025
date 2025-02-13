package com.show_rural.hackathon.controller.dto;

import lombok.Data;

@Data
public class PageParams {
    private Integer limit = 10;
    private Integer offset = 0;
}
