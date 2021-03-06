package de.teamlapen.vampirism.core;

import de.teamlapen.lib.lib.util.BasicCommand;
import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.lib.lib.util.VersionChecker;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.config.Balance;
import de.teamlapen.vampirism.config.Configs;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Central command for this mod
 */
public class VampirismCommand extends BasicCommand {

    public VampirismCommand() {
        if (VampirismMod.inDev) {
            aliases.add("v");
        }
        final IPlayableFaction[] pfactions = VampirismAPI.factionRegistry().getPlayableFactions();
        final String[] pfaction_names = new String[pfactions.length];
        for (int i = 0; i < pfactions.length; i++) {
            pfaction_names[i] = pfactions[i].name();
        }
        addSub(new SubCommand() {
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return (args.length == 1) ? getListOfStringsMatchingLastWord(args, getCategories()) : Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() || sender.canCommandSenderUseCommand(3, getCommandName());
            }

            @Override
            public String getCommandName() {
                return "resetBalance";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName() + " <all/[category]/help>";
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) {
                String cat;
                if (args == null || args.length == 0) {
                    cat = "all";
                } else {
                    cat = args[0];
                }
                if ("help".equals(cat)) {
                    sender.addChatMessage(new TextComponentString("You can reset Vampirism balance values to the default values. If you have not modified them, this is recommend after every update of Vampirism"));
                    sender.addChatMessage(new TextComponentString("Use '/vampirism resetBalance all' to reset all categories or specify a category with '/vampirism resetBalance <category>' (Tab completion is supported)"));
                }
                boolean p = Balance.resetAndReload(cat);
                if (p) {
                    sender.addChatMessage(new TextComponentString("Successfully reset " + cat + " balance category. Please restart MC."));
                } else {
                    sender.addChatMessage(new TextComponentString("Did not find " + cat + " balance category."));
                }
            }

            private String[] getCategories() {
                Set<String> categories = Balance.getCategories().keySet();
                String[] result = categories.toArray(new String[categories.size() + 2]);
                result[result.length - 1] = "all";
                result[result.length - 2] = "help";
                return result;
            }
        });
        addSub(new SubCommand() {

            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return args.length == 1 ? getListOfStringsMatchingLastWord(args, pfaction_names) : Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return canCommandSenderUseCheatCommand(sender);
            }

            @Override
            public String getCommandName() {
                return "level";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {

                return getCommandName() + " " + ArrayUtils.toString(pfaction_names) + " <level> [<player>]";
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if (args.length < 2 || args.length > 3) {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                EntityPlayer player = args.length == 3 ? getPlayer(server, sender, args[2]) : getCommandSenderAsPlayer(sender);
                int level;
                    try {
                        level = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        throw new NumberInvalidException();
                    }
                    //Search factions
                    for (int i = 0; i < pfaction_names.length; i++) {
                        if (pfaction_names[i].equalsIgnoreCase(args[0])) {
                            IPlayableFaction newFaction = pfactions[i];
                            FactionPlayerHandler handler = FactionPlayerHandler.get(player);
                            if (level == 0 && !handler.canLeaveFaction()) {
                                sender.addChatMessage(new TextComponentTranslation("text.vampirism.faction.cant_leave").appendSibling(new TextComponentString("(" + handler.getCurrentFaction().name() + ")")));
                                return;
                            }
                            if (level > newFaction.getHighestReachableLevel()) {
                                level = newFaction.getHighestReachableLevel();
                            }
                            if (handler.setFactionAndLevel(newFaction, level)) {
                                ITextComponent msg = player.getDisplayName().appendSibling(new TextComponentString(" is now a " + pfaction_names[i] + " level " + level));
                                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendChatMsg(msg);
                            } else {
                                sender.addChatMessage(new TextComponentTranslation("text.vampirism.faction.failed_to_change"));
                            }

                            return;
                        }
                    }
                throw new CommandException("commands.vampirism.level.faction_not_found", args[0]);


            }
        });
        addSub(new SubCommand() {
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return null;
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return sender instanceof EntityPlayer;
            }

            @Override
            public String getCommandName() {
                return "eye";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName() + " <id [0-" + (REFERENCE.EYE_TYPE_COUNT - 1) + "]> ";
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                EntityPlayer player = (EntityPlayer) sender;
                if (args.length != 1) {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                try {
                    int type = Integer.parseInt(args[0]);
                    if (!VampirePlayer.get(player).setEyeType(type)) {
                        sendMessage(sender, "<id> has to be a valid number between 0 and " + (REFERENCE.EYE_TYPE_COUNT - 1));
                    }
                } catch (NumberFormatException e) {
                    throw new NumberInvalidException();
                }
            }
        });
        addSub(new SubCommand() {
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return null;
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return sender instanceof EntityPlayer;
            }

            @Override
            public String getCommandName() {
                return "fang";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName() + " <id [0-" + (REFERENCE.FANG_TYPE_COUNT - 1) + "]> ";
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                EntityPlayer player = (EntityPlayer) sender;
                if (args.length != 1) {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
                try {
                    int type = Integer.parseInt(args[0]);
                    if (!VampirePlayer.get(player).setFangType(type)) {
                        sendMessage(sender, "<id> has to be a valid number between 0 and " + (REFERENCE.FANG_TYPE_COUNT - 1));
                    }
                } catch (NumberFormatException e) {
                    throw new NumberInvalidException();
                }
            }
        });

        addSub(new SubCommand() {
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return canCommandSenderUseCommand(sender, PERMISSION_LEVEL_FULL, getCommandName());
            }

            @Override
            public String getCommandName() {
                return "checkForVampireBiome";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName() + " <maxRadius in chunks>";
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) {
                if (Configs.disable_vampireForest) {
                    sender.addChatMessage(new TextComponentString("The Vampire Biome is disabled in the config file"));
                } else {
                    int maxDist = 150;
                    if (args.length > 0) {
                        try {
                            maxDist = Integer.parseInt(args[0]);
                        } catch (NumberFormatException e) {
                            VampirismMod.log.w("CheckVampireBiome", "Failed to parse max dist %s", args[0]);
                            sender.addChatMessage(new TextComponentString("Failed to parse max distance. Using " + maxDist));
                        }
                        if (maxDist > 500) {
                            if (args.length > 1 && "yes".equals(args[1])) {

                            } else {
                                sender.addChatMessage(new TextComponentString("This will take a long time. Please use '/" + getCommandUsage(sender) + " yes', if you are sure"));
                                return;
                            }
                        }
                    }
                    List<Biome> biomes = new ArrayList<Biome>();
                    biomes.add(ModBiomes.vampireForest);
                    sender.addChatMessage(new TextComponentTranslation("text.vampirism.biome.looking_for_biome"));
                    ChunkPos pos = UtilLib.findNearBiome(sender.getEntityWorld(), (sender).getPosition(), maxDist, biomes, sender);
                    if (pos == null) {
                        sender.addChatMessage(new TextComponentTranslation("text.vampirism.biome.not_found"));
                    } else {
                        sender.addChatMessage(new TextComponentTranslation("text.vampirism.biome.found").appendSibling(new TextComponentString("[" + (pos.chunkXPos << 4) + "," + (pos.chunkZPos << 4) + "]")));
                    }
                }
            }
        });
        addSub(new SubCommand() {
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public String getCommandName() {
                return "changelog";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName();
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) {
                if (!VampirismMod.instance.getVersionInfo().isNewVersionAvailable()) {
                    sender.addChatMessage(new TextComponentString("There is no new version available"));
                    return;
                }
                VersionChecker.Version newVersion = VampirismMod.instance.getVersionInfo().getNewVersion();
                List<String> changes = newVersion.getChanges();
                sender.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Vampirism " + newVersion.name + "(" + MinecraftForge.MC_VERSION + ")"));
                for (String c : changes) {
                    sender.addChatMessage(new TextComponentString("-" + c));
                }
                sender.addChatMessage(new TextComponentString(""));
                String template = I18n.translateToLocal("text.vampirism.update_message");
                String homepage = VampirismMod.instance.getVersionInfo().getHomePage();
                template = template.replaceAll("@download@", newVersion.getUrl() == null ? homepage : newVersion.getUrl()).replaceAll("@forum@", homepage);
                ITextComponent component = ITextComponent.Serializer.jsonToComponent(template);
                sender.addChatMessage(component);
            }
        });
        addSub(new SubCommand() {


            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public String getCommandName() {
                return "currentDimension";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName();
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) {
                if (sender instanceof EntityPlayer) {
                    EntityPlayer p = (EntityPlayer) sender;
                    if (p.worldObj != null) {
                        sender.addChatMessage(new TextComponentString("Dimension ID: " + p.worldObj.provider.getDimension()));
                    }
                }
            }
        });
        addSub(new SubCommand() {
            @Nonnull
            @Override
            public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                return Collections.emptyList();
            }

            @Override
            public boolean canSenderUseCommand(ICommandSender sender) {
                return !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer() || sender.canCommandSenderUseCommand(3, getCommandName());
            }

            @Override
            public String getCommandName() {
                return "debug";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return getCommandName();
            }

            @Override
            public void processCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                boolean enabled = VampirismMod.log.isDebug();
                VampirismMod.log.setDebug(!enabled);
                String msg = enabled ? "Disabled debug mode" : "Enabled debug mode";
                sender.addChatMessage(new TextComponentString(msg));
            }
        });
    }

    @Override
    public String getCommandName() {
        return "vampirism";
    }

    @Override
    protected boolean canCommandSenderUseCheatCommand(ICommandSender sender) {
        if (VampirismMod.inDev) {
            return true;
        }
        return super.canCommandSenderUseCheatCommand(sender);
    }

    protected boolean canCommandSenderUseCommand(ICommandSender sender, int perm, String command) {
        if (VampirismMod.inDev) {
            return true;
        }
        return sender.canCommandSenderUseCommand(perm, command);
    }
}
