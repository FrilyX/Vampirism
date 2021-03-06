package de.teamlapen.vampirism.entity.ai;

import de.teamlapen.vampirism.api.entity.vampire.IVampireMob;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import java.util.List;


public class VampireAIMoveToBiteable extends EntityAIBase {


    private final IVampireMob vampire;
    private final EntityLiving vampireEntity;
    private final double movementSpeed;
    private EntityCreature target;
    private int timeout;

    /**
     * @param vampire       Has to be a {@link EntityLiving}
     * @param movementSpeed
     */
    public VampireAIMoveToBiteable(IVampireMob vampire, double movementSpeed) {
        this.vampire = vampire;
        this.vampireEntity = (EntityLiving) vampire.getRepresentingEntity();
        this.movementSpeed = movementSpeed;
        this.setMutexBits(1);
    }

    @Override
    public boolean continueExecuting() {
        return (!this.vampireEntity.getNavigator().noPath() && !target.isDead);
    }

    @Override
    public void resetTask() {
        target = null;
        timeout = (vampireEntity.getRNG().nextInt(5) == 0 ? 80 : 3);
    }


    @Override
    public boolean shouldExecute() {
        if (timeout > 0) {
            timeout--;
            return false;
        }
        if (!vampire.wantsBlood()) return false;
        List list = vampireEntity.worldObj.getEntitiesWithinAABB(EntityCreature.class, vampireEntity.getEntityBoundingBox().expand(10, 3, 10));
        for (Object o : list) {
            if (ExtendedCreature.get((EntityCreature) o).canBeBitten(vampire)) {
                target = (EntityCreature) o;
                return true;
            }
        }
        target = null;
        return false;
    }

    @Override
    public void startExecuting() {
        vampireEntity.getNavigator().tryMoveToEntityLiving(target, 1.0);
    }
}
