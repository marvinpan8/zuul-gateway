package com.marvinpan.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenCheckVo {

    private String tenantId;

    private Boolean expired = Boolean.TRUE;
}
