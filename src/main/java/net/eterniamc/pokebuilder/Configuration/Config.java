package net.eterniamc.pokebuilder.Configuration;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public class Config {

    @Setting
    public static double shinyModifierCost = 500;

    @Setting
    public static double hpIvsCost = 500;

    @Setting
    public static double attIvsCost = 500;

    @Setting
    public static double defIvsCost = 500;

    @Setting
    public static double spAttIvsCost = 500;

    @Setting
    public static double spDefIvsCost = 500;

    @Setting
    public static double speedIvsCost = 500;

    @Setting
    public static double rerollIvsCost = 500;

    @Setting
    public static double maxIvsCost = 500;

    @Setting
    public static double hpEvsCost = 500;

    @Setting
    public static double attEvsCost = 500;

    @Setting
    public static double defEvsCost = 500;

    @Setting
    public static double spAttEvsCost = 500;

    @Setting
    public static double spDefEvsCost = 500;

    @Setting
    public static double speedEvsCost = 500;

    @Setting
    public static double resetEvsCost = 500;

    @Setting
    public static double randomMaxEvsCost = 500;

    @Setting
    public static double hiddenAbilityModifierCost = 500;

    @Setting
    public static double natureModifierCost = 500;

    @Setting
    public static double genderModifierCost = 500;

    @Setting
    public static double pokeballModifierCost = 500;

    @Setting
    public static double moveModifierCost = 500;

    @Setting
    public static double growthModifierCost = 500;

    @Setting
    public static double maxHappinessModifierCost = 500;

    @Setting
    public static double pokemonCost = 1000;

    @Setting
    public static double legendaryCost = 1500;

    @Setting
    public static double legendaryOrDittoMultiplier = 1.5;

    @Setting
    public static String currencyName = "Coin";

    @Setting
    public static List<String> blacklistedPokemon = new ArrayList<>();

}
