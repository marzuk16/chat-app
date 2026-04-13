package com.marzuk.components.pojos.dto.user;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {
    private UUID id;
    private String username;
    private String displayName;
    private String avatarUrl;
}
