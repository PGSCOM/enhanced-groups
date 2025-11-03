package de.maxhenkel.enhancedgroups;

import de.maxhenkel.admiral.MinecraftAdmiral;
import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.enhancedgroups.command.*;
import de.maxhenkel.enhancedgroups.config.AutoJoinGroupStore;
import de.maxhenkel.enhancedgroups.config.CommonConfig;
import de.maxhenkel.enhancedgroups.config.PersistentGroupStore;
import de.maxhenkel.enhancedgroups.events.GroupSummaryEvents;
import de.maxhenkel.voicechat.api.Group;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(EnhancedGroups.MOD_ID)
public class EnhancedGroups {

    public static final String MOD_ID = "enhancedgroups";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static CommonConfig CONFIG;
    public static PersistentGroupStore PERSISTENT_GROUP_STORE;
    public static AutoJoinGroupStore AUTO_JOIN_GROUP_STORE;

    public static EnhancedGroupPermissionManager PERMISSION_MANAGER;

    public EnhancedGroups(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Path configFolder = Paths.get(".", "config").resolve(MOD_ID);
        CONFIG = ConfigBuilder.builder(CommonConfig::new).path(configFolder.resolve("%s.properties".formatted(MOD_ID))).build();
        PERSISTENT_GROUP_STORE = new PersistentGroupStore(configFolder.resolve("persistent-groups.json").toFile());
        AUTO_JOIN_GROUP_STORE = new AutoJoinGroupStore(configFolder.resolve("auto-join-groups.json").toFile());
        PERMISSION_MANAGER = new EnhancedGroupPermissionManager();
        
        GroupSummaryEvents.init();
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MinecraftAdmiral.builder(event.getDispatcher(), event.getBuildContext())
                .addCommandClasses(
                        AutoJoinGroupCommands.class,
                        AutoJoinGroupGlobalCommands.class,
                        ForceJoinCommands.class,
                        InstantGroupCommands.class,
                        PersistentGroupCommands.class
                )
                .setPermissionManager(PERMISSION_MANAGER)
                .addArgumentTypes(argumentTypeRegistry -> argumentTypeRegistry.register(Group.Type.class, new GroupTypeArgumentSupplier(), new GroupTypeArgumentTypeSupplier()))
                .build();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        PERSISTENT_GROUP_STORE.clearCache();
    }
}
