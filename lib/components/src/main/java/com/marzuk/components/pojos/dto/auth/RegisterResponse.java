package com.marzuk.components.pojos.dto.auth;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private UUID userId;
}
