package de.teamlapen.vampirism.player.vampire.actions;

import de.teamlapen.vampirism.api.entity.player.actions.ILastingAction;
import de.teamlapen.vampirism.api.entity.player.vampire.DefaultVampireAction;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.config.Balance;


public class InvisibilityVampireAction extends DefaultVampireAction implements ILastingAction<IVampirePlayer> {
    public InvisibilityVampireAction() {
        super(null);
    }


    @Override
    public int getCooldown() {
        return Balance.vpa.INVISIBILITY_COOLDOWN * 20;
    }

    @Override
    public int getDuration(int level) {
        return Balance.vpa.INVISIBILITY_DURATION * 20;
    }


    @Override
    public int getMinU() {
        return 128;
    }

    @Override
    public int getMinV() {
        return 0;
    }

    @Override
    public String getUnlocalizedName() {
        return "action.vampirism.vampire.invisibility";
    }

    @Override
    public boolean isEnabled() {
        return Balance.vpa.INVISIBILITY_ENABLED;
    }

    @Override
    public boolean onActivated(IVampirePlayer vampire) {
        vampire.getRepresentingPlayer().setInvisible(true);
        return true;
    }

    @Override
    public void onActivatedClient(IVampirePlayer vampire) {

    }

    @Override
    public void onDeactivated(IVampirePlayer vampire) {
        vampire.getRepresentingPlayer().setInvisible(false);
    }

    @Override
    public void onReActivated(IVampirePlayer vampire) {
        onActivated(vampire);
    }

    @Override
    public boolean onUpdate(IVampirePlayer vampire) {
        if (!vampire.getRepresentingPlayer().isInvisible()) {
            vampire.getRepresentingPlayer().setInvisible(true);
        }
        return false;
    }
}
