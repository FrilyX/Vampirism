package de.teamlapen.vampirism.entity.ai;

import com.google.common.collect.Lists;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

import java.util.List;

/**
 * Almost the same as {@link EntityAIMoveThroughVillage},
 * but I had to reimplement the whole class since {@link EntityAIMoveThroughVillage#resizeDoorList()}
 * only works for villages with more than 15 doors and is private.
 */
public class EntityAIMoveThroughVillageCustom extends EntityAIBase {
    private final int randomTicksToResize;
    private EntityCreature theEntity;
    private double movementSpeed;
    /**
     * The PathNavigate of our entity.
     */
    private Path entityPathNavigate;
    private VillageDoorInfo doorInfo;
    private boolean isNocturnal;
    private List<VillageDoorInfo> doorList = Lists.newArrayList();

    /**
     * @param theEntityIn         The entity
     * @param movementSpeedIn     Relative movement speed
     * @param isNocturnalIn       If true, the task won't execute during daytime
     * @param randomTicksToResize How often the list of blacklisted doors is shrinked. (Indirectly affects how often this tasks is executed)
     */
    public EntityAIMoveThroughVillageCustom(EntityCreature theEntityIn, double movementSpeedIn, boolean isNocturnalIn, int randomTicksToResize) {
        this.theEntity = theEntityIn;
        this.movementSpeed = movementSpeedIn;
        this.isNocturnal = isNocturnalIn;
        this.randomTicksToResize = randomTicksToResize;
        this.setMutexBits(1);

        if (!(theEntityIn.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        if (this.theEntity.getNavigator().noPath()) {
            return false;
        } else {
            float f = this.theEntity.width + 4.0F;
            return this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) > (double) (f * f);
        }
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        if (this.theEntity.getNavigator().noPath() || this.theEntity.getDistanceSq(this.doorInfo.getDoorBlockPos()) < 16.0D) {
            this.doorList.add(this.doorInfo);
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        this.resizeDoorList();

        if (this.isNocturnal && this.theEntity.worldObj.isDaytime()) {
            return false;
        } else {
            Village village = this.theEntity.worldObj.getVillageCollection().getNearestVillage(new BlockPos(this.theEntity), 0);

            if (village == null) {
                return false;
            } else {
                this.doorInfo = this.findNearestDoor(village);

                if (this.doorInfo == null) {
                    return false;
                } else {
                    PathNavigateGround pathnavigateground = (PathNavigateGround) this.theEntity.getNavigator();
                    boolean flag = pathnavigateground.getEnterDoors();
                    pathnavigateground.setBreakDoors(false);
                    this.entityPathNavigate = pathnavigateground.getPathToPos(this.doorInfo.getDoorBlockPos());
                    pathnavigateground.setBreakDoors(flag);

                    if (this.entityPathNavigate != null) {

                        return true;
                    } else {
                        Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.theEntity, 10, 7, new Vec3d((double) this.doorInfo.getDoorBlockPos().getX(), (double) this.doorInfo.getDoorBlockPos().getY(), (double) this.doorInfo.getDoorBlockPos().getZ()));

                        if (vec3d == null) {
                            return false;
                        } else {

                            pathnavigateground.setBreakDoors(false);
                            this.entityPathNavigate = this.theEntity.getNavigator().getPathToXYZ(vec3d.xCoord, vec3d.yCoord, vec3d.zCoord);
                            pathnavigateground.setBreakDoors(flag);

                            return this.entityPathNavigate != null;
                        }
                    }
                }
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.theEntity.getNavigator().setPath(this.entityPathNavigate, this.movementSpeed);
    }

    private boolean doesDoorListContain(VillageDoorInfo doorInfoIn) {
        for (VillageDoorInfo villagedoorinfo : this.doorList) {
            if (doorInfoIn.getDoorBlockPos().equals(villagedoorinfo.getDoorBlockPos())) {
                return true;
            }
        }

        return false;
    }

    private VillageDoorInfo findNearestDoor(Village villageIn) {
        VillageDoorInfo villagedoorinfo = null;
        int i = Integer.MAX_VALUE;

        for (VillageDoorInfo villagedoorinfo1 : villageIn.getVillageDoorInfoList()) {
            int j = villagedoorinfo1.getDistanceSquared(MathHelper.floor_double(this.theEntity.posX), MathHelper.floor_double(this.theEntity.posY), MathHelper.floor_double(this.theEntity.posZ));

            if (j < i && !this.doesDoorListContain(villagedoorinfo1)) {
                villagedoorinfo = villagedoorinfo1;
                i = j;
            }
        }

        return villagedoorinfo;
    }

    private void resizeDoorList() {
        if (this.doorList.size() > 5 && this.theEntity.getRNG().nextInt(randomTicksToResize) == 0) {
            this.doorList.remove(0);
        }
    }
}
