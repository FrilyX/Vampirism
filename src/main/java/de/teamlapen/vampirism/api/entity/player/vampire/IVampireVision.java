package de.teamlapen.vampirism.api.entity.player.vampire;

/**
 * Interface for Vampire Player's "vision", e.g. night vision or blood vision
 */
public interface IVampireVision {

    String getUnlocName();

    void onActivated(IVampirePlayer player);

    void onDeactivated(IVampirePlayer player);

    void onUpdate(IVampirePlayer player);
}
