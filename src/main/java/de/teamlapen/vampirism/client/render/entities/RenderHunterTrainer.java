package de.teamlapen.vampirism.client.render.entities;

import de.teamlapen.vampirism.client.model.ModelBasicHunter;
import de.teamlapen.vampirism.entity.hunter.EntityHunterTrainer;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHunterTrainer extends RenderBiped<EntityHunterTrainer> {
    private final ResourceLocation texture = new ResourceLocation(REFERENCE.MODID, "textures/entity/vampireHunterBase1.png");

    private final ResourceLocation textureExtra = new ResourceLocation(REFERENCE.MODID, "textures/entity/vampireHunterExtra.png");

    public RenderHunterTrainer(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBasicHunter(), 0.5F);
    }

    @Override
    public void doRender(EntityHunterTrainer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHunterTrainer entity) {
        return texture;
    }

    @Override
    protected void renderModel(EntityHunterTrainer entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float partTicks) {
        super.renderModel(entitylivingbaseIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, partTicks);
        bindTexture(textureExtra);
        ((ModelBasicHunter) modelBipedMain).renderHat(partTicks, 1);

    }
}
