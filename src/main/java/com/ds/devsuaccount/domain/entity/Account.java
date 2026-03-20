package com.ds.devsuaccount.domain.entity;

import com.ds.devsuaccount.domain.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class Account {
    private String id;
    private String type;
}
