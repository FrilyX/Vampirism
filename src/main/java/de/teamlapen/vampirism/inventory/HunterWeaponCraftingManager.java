package de.teamlapen.vampirism.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.items.IHunterWeaponCraftingManager;
import de.teamlapen.vampirism.api.items.IHunterWeaponRecipe;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;


public class HunterWeaponCraftingManager implements IHunterWeaponCraftingManager {

    private final static String TAG = "HWCraftingManager";
    private static final HunterWeaponCraftingManager INSTANCE = new HunterWeaponCraftingManager();

    public static HunterWeaponCraftingManager getInstance() {
        return INSTANCE;
    }

    private final List<IHunterWeaponRecipe> recipes = Lists.newLinkedList();

    public HunterWeaponCraftingManager() {

    }

    @Override
    public ShapedHunterWeaponRecipe addRecipe(ItemStack output, int reqLevel, @Nullable ISkill<IHunterPlayer> reqSkill, Object... recipeComponents) {
        String s = "";
        int i = 0;
        int j = 0;
        int k = 0;

        if (recipeComponents[i] instanceof String[]) {
            String[] astring = (String[]) recipeComponents[i++];

            for (int l = 0; l < astring.length; ++l) {
                String s2 = astring[l];
                ++k;
                j = s2.length();
                s = s + s2;
            }
        } else {
            while (recipeComponents[i] instanceof String) {
                String s1 = (String) recipeComponents[i++];
                ++k;
                j = s1.length();
                s = s + s1;
            }
        }

        Map<Character, ItemStack> map;

        for (map = Maps.newHashMap(); i < recipeComponents.length; i += 2) {
            Character character = (Character) recipeComponents[i];
            ItemStack itemstack = null;

            if (recipeComponents[i + 1] instanceof Item) {
                itemstack = new ItemStack((Item) recipeComponents[i + 1]);
            } else if (recipeComponents[i + 1] instanceof Block) {
                itemstack = new ItemStack((Block) recipeComponents[i + 1], 1, 32767);
            } else if (recipeComponents[i + 1] instanceof ItemStack) {
                itemstack = (ItemStack) recipeComponents[i + 1];
            } else {
                VampirismMod.log.e(TAG, "Cannot add %s to recipe as %s since it is not supported", recipeComponents[i + 1], character);
            }

            map.put(character, itemstack);
        }
        ItemStack[] aitemstack = new ItemStack[j * k];

        for (int i1 = 0; i1 < j * k; ++i1) {
            char c0 = s.charAt(i1);

            if (map.containsKey(c0)) {
                aitemstack[i1] = (map.get(c0)).copy();
            } else {
                aitemstack[i1] = null;
            }
        }

        ShapedHunterWeaponRecipe recipe = new ShapedHunterWeaponRecipe(aitemstack, output, reqLevel, reqSkill);
        this.recipes.add(recipe);
        return recipe;
    }

    @Override
    public void addRecipe(IHunterWeaponRecipe recipe) {
        this.recipes.add(recipe);
    }

    @Override
    public
    @Nullable
    ItemStack findMatchingRecipe(InventoryCrafting craftMatrix, World world, int playerLevel, ISkillHandler<IHunterPlayer> skillHandler) {
        for (IHunterWeaponRecipe iRecipe : this.recipes) {
            if (iRecipe.matches(craftMatrix, world)) {
                if (playerLevel >= iRecipe.getMinHunterLevel() && (iRecipe.getRequiredSkill() == null || skillHandler.isSkillEnabled(iRecipe.getRequiredSkill()))) {
                    return iRecipe.getCraftingResult(craftMatrix);
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting craftMatrix, World world, int playerLevel, ISkillHandler<IHunterPlayer> skillHandler) {
        for (IHunterWeaponRecipe iRecipe : this.recipes) {
            if (iRecipe.matches(craftMatrix, world)) {
                if (playerLevel >= iRecipe.getMinHunterLevel() && (iRecipe.getRequiredSkill() == null || skillHandler.isSkillEnabled(iRecipe.getRequiredSkill()))) {
                    return iRecipe.getRemainingItems(craftMatrix);

                }
            }
        }

        ItemStack[] aitemstack = new ItemStack[craftMatrix.getSizeInventory()];

        for (int i = 0; i < aitemstack.length; ++i) {
            aitemstack[i] = craftMatrix.getStackInSlot(i);
        }

        return aitemstack;
    }
}