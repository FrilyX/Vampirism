package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.player.hunter.skills.HunterSkills;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class ItemDoubleCrossbow extends ItemSimpleCrossbow {
    /**
     * @param regName       Registry name
     * @param speed         Speed of the shot arrows (0.1F-20F)
     * @param coolDownTicks Cooldown ticks >0
     * @param maxDamage Max amount of shot arrrows or 0 if unbreakable
     */
    public ItemDoubleCrossbow(String regName, float speed, int coolDownTicks, int maxDamage) {
        super(regName, speed, coolDownTicks, maxDamage);
    }

    @Nullable
    @Override
    public ISkill<IHunterPlayer> getRequiredSkill(ItemStack stack) {
        return HunterSkills.doubleCrossbow;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        shoot(playerIn, heightOffset, worldIn, itemStackIn);
        shoot(playerIn, heightOffset - 0.2, worldIn, itemStackIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }

    @Override
    protected boolean isIgnoreHurtTime(ItemStack crossbow) {
        return true;
    }
}
