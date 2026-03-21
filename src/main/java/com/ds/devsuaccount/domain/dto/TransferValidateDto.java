package com.ds.devsuaccount.domain.dto;

import com.ds.devsuaccount.domain.entity.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferValidateDto {
    private Boolean isValid;
    private Transfer transfer;
}
