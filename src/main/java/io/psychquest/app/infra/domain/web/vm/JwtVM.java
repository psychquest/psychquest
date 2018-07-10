package io.psychquest.app.infra.domain.web.vm;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class JwtVM {
    @JsonProperty("id_token")
    private final String idToken;
}
