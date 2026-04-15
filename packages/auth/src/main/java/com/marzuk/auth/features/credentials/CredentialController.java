package com.marzuk.auth.features.credentials;

import com.marzuk.auth.common.AuthRoutes;
import com.marzuk.components.constants.AppHeaders;
import com.marzuk.components.pojos.dto.auth.RegisterRequest;
import com.marzuk.components.pojos.dto.auth.RegisterResponse;
import com.marzuk.components.pojos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthRoutes.BASE)
@RequiredArgsConstructor
@Tag(name = "Credentials")
public class CredentialController {

    private final CredentialService credentialService;

    @PostMapping(AuthRoutes.REGISTER)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public Response<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(AppHeaders.USER_ID) UUID actorId) {
        return Response.success(credentialService.register(request, actorId));
    }
}
