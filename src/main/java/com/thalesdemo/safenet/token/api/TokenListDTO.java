package com.thalesdemo.safenet.token.api;

import lombok.Data;
import java.util.List;

@Data
public class TokenListDTO {
    private String owner;
    private List<TokenDTO> tokens;

    // Lombok @Data generates constructor, getters, setters, equals, hashCode, and
    // toString methods.
}
