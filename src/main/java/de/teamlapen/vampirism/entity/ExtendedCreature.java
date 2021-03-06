package de.teamlapen.vampirism.entity;

import de.teamlapen.lib.HelperLib;
import de.teamlapen.lib.lib.network.ISyncable;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.BiteableEntry;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.api.entity.convertible.IConvertedCreature;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.api.world.IVampirismVillage;
import de.teamlapen.vampirism.config.Balance;
import de.teamlapen.vampirism.potion.PotionSanguinare;
import de.teamlapen.vampirism.util.REFERENCE;
import de.teamlapen.vampirism.world.villages.VampirismVillageCollection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;

/**
 * Extended entity property which every {@link EntityCreature} has
 */
public class ExtendedCreature implements ISyncable.ISyncableEntityCapabilityInst, IExtendedCreatureVampirism {

    @CapabilityInject(IExtendedCreatureVampirism.class)
    public static final Capability<IExtendedCreatureVampirism> CAP = null;
    private static final String TAG = "ExtendedCreature";
    private final static String KEY_BLOOD = "bloodLevel";

    public static ExtendedCreature get(EntityCreature mob) {
        return (ExtendedCreature) mob.getCapability(CAP, null);
    }


    public static void registerCapability() {
        CapabilityManager.INSTANCE.register(IExtendedCreatureVampirism.class, new Storage(), ExtendedCreatureDefaultImpl.class);
    }

    @SuppressWarnings("ConstantConditions")
    public static ICapabilityProvider createNewCapability(final EntityCreature creature) {
        return new ICapabilitySerializable<NBTTagCompound>() {

            IExtendedCreatureVampirism inst = new ExtendedCreature(creature);

            @Override
            public void deserializeNBT(NBTTagCompound nbt) {
                CAP.getStorage().readNBT(CAP, inst, null, nbt);
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
                return CAP.equals(capability) ? CAP.<T>cast(inst) : null;
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
                return CAP.equals(capability);
            }

            @Override
            public NBTTagCompound serializeNBT() {
                return (NBTTagCompound) CAP.getStorage().writeNBT(CAP, inst, null);
            }
        };
    }

    private final EntityCreature entity;
    private final int maxBlood;
    private final boolean canBecomeVampire;
    /**
     * Stores the current blood value.
     * If this is -1, this entity never had any blood and this value cannot be changed
     */
    private int blood;

    public ExtendedCreature(EntityCreature entity) {
        this.entity = entity;
        BiteableEntry entry = VampirismAPI.biteableRegistry().getEntry(entity);
        if (entry != null) {
            maxBlood = entry.blood;
            canBecomeVampire = entry.convertible;
        } else {
            maxBlood = -1;
            canBecomeVampire = false;
        }
        blood = maxBlood;
    }

    @Override
    public boolean canBeBitten(IVampire biter) {
        return getBlood() > 0;
    }

    public boolean canBecomeVampire() {
        return canBecomeVampire;
    }

    @Override
    public int getBlood() {
        return blood;
    }

    @Override
    public void setBlood(int blood) {
        if (blood >= 0 && blood <= getMaxBlood()) {
            if (getBlood() != -1) {
                this.blood = blood;
            }

        }
    }

    @Override
    public float getBloodSaturation() {
        return 1.0F;//TODO adjust
    }

    @Override
    public ResourceLocation getCapKey() {
        return REFERENCE.EXTENDED_CREATURE_KEY;
    }

    @Override
    public EntityCreature getEntity() {
        return entity;
    }

    @Override
    public int getMaxBlood() {
        return maxBlood;
    }

    @Override
    public int getTheEntityID() {
        return entity.getEntityId();
    }

    @Override
    public void loadUpdateFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(KEY_BLOOD)) {
            setBlood(nbt.getInteger(KEY_BLOOD));
        }
    }

    @Override
    public
    @Nullable
    IConvertedCreature makeVampire() {
        if (canBecomeVampire()) {
            blood = 0;
            IConvertedCreature c = VampirismAPI.biteableRegistry().convert(entity);
            if (c != null) {
                Entity e = (Entity) c;
                entity.setDead();
                entity.worldObj.spawnEntityInWorld(e);
            }
            return c;
        }
        return null;
    }

    @Override
    public int onBite(IVampire biter) {
        if (getBlood() <= 0) return 0;
        int amt = Math.min(blood, (int) (getMaxBlood() / 2F));
        blood -= amt;
        boolean killed = false;
        boolean converted = false;
        if (blood < getMaxBlood() / 2) {
            if (blood == 0 || entity.getRNG().nextInt(blood + 1) == 0) {

                if (canBecomeVampire && entity.getRNG().nextBoolean()) {
                    if (VampirismMod.isRealism()) {
                        PotionSanguinare.addRandom(entity, false);
                    } else {
                        makeVampire();
                    }
                    converted = true;

                } else {
                    entity.attackEntityFrom(DamageSource.magic, 1000);
                    killed = true;
                }
            }

        }

        // If entity is a child only give 1/3 blood
        if (entity instanceof EntityAgeable) {
            if (((EntityAgeable) entity).getGrowingAge() < 0) {
                return Math.round((float) amt / 3);
            }
        }
        this.sync();
        if (amt > 0 && entity instanceof EntityVillager) {
            IVampirismVillage vv = VampirismVillageCollection.get((entity).worldObj).getNearestVillage(entity);
            if (vv != null) {
                vv.onVillagerBitten(biter);
                if (converted) {
                    vv.onVillagerConverted(biter);
                }
                if (killed) {
                    vv.onVillagerBittenToDeath(biter);
                }
            }
        }
        return amt;
    }

    /**
     * Called every tick
     */
    public void onUpdate() {
        if (!entity.worldObj.isRemote) {
            if (blood > 0 && blood < getMaxBlood() && entity.ticksExisted % 40 == 8) {
                entity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 41));
                entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 41, 2));
                if (entity.getRNG().nextInt(Balance.mobProps.BLOOD_REGEN_CHANCE) == 0) {
                    setBlood(getBlood() + 1);
                }
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + " for entity (" + entity.toString() + ") [B" + blood + ",MB" + maxBlood + ",CV" + canBecomeVampire + "]";
    }

    @Override
    public void writeFullUpdateToNBT(NBTTagCompound nbt) {
        nbt.setInteger(KEY_BLOOD, getBlood());
    }

    private void loadNBTData(NBTTagCompound compound) {
        if (compound.hasKey(KEY_BLOOD)) {
            setBlood(compound.getInteger(KEY_BLOOD));
        }

    }

    private void saveNBTData(NBTTagCompound compound) {
        compound.setInteger(KEY_BLOOD, blood);
    }

    private void sync() {
        HelperLib.sync(this, getEntity(), false);
    }

    private void sync(NBTTagCompound data) {
        HelperLib.sync(this, data, getEntity(), false);

    }

    private static class Storage implements Capability.IStorage<IExtendedCreatureVampirism> {
        @Override
        public void readNBT(Capability<IExtendedCreatureVampirism> capability, IExtendedCreatureVampirism instance, EnumFacing side, NBTBase nbt) {
            ((ExtendedCreature) instance).loadNBTData((NBTTagCompound) nbt);
        }

        @Override
        public NBTBase writeNBT(Capability<IExtendedCreatureVampirism> capability, IExtendedCreatureVampirism instance, EnumFacing side) {
            NBTTagCompound nbt = new NBTTagCompound();
            ((ExtendedCreature) instance).saveNBTData(nbt);
            return nbt;
        }
    }
}
