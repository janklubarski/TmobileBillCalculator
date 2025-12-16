package com.phonecompany.billing.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TelephoneCall {

    private Long telephoneNumber;

    private LocalDateTime callStart;

    private LocalDateTime callEnd;


}
