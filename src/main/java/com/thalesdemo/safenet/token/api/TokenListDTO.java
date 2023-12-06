package com.thalesdemo.safenet.token.api;

import java.util.ArrayList;
import java.util.List;

public class TokenListDTO {
    private String owner;
    private List<String> serials = new ArrayList<>();
    private List<String> types = new ArrayList<>();
    private List<String> options = new ArrayList<>();

    public TokenListDTO() {
    }

    public TokenListDTO(List<String> serials, String owner) {
        this.serials = serials;
        this.owner = owner;
    }

    public List<String> getTypes() {
        return types;
    }

    public void addType(String type) {
        this.types.add(type);
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getSerials() {
        return serials;
    }

    public void setSerials(List<String> serials) {
        this.serials = serials;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void addSerial(String serial) {
        this.serials.add(serial);
    }

    public List<String> getOptions() {
        return options;
    }

    public void addOption(String option) {
        this.options.add(option);
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

}
