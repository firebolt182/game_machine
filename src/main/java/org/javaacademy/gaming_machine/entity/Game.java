package org.javaacademy.gaming_machine.entity;

import lombok.Data;

@Data
public class Game {
    private int id;
    private String firstSymbol;
    private String secondSymbol;
    private String thirdSymbol;
}
