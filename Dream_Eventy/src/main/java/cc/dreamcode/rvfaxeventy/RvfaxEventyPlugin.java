package cc.dreamcode.rvfaxeventy;

import cc.dreamcode.command.bukkit.BukkitCommandProvider;
import cc.dreamcode.menu.bukkit.BukkitMenuProvider;
import cc.dreamcode.menu.serializer.MenuBuilderSerializer;
import cc.dreamcode.notice.serializer.BukkitNoticeSerializer;
import cc.dreamcode.platform.DreamVersion;
import cc.dreamcode.platform.bukkit.DreamBukkitConfig;
import cc.dreamcode.platform.bukkit.DreamBukkitPlatform;
import cc.dreamcode.platform.bukkit.component.ConfigurationResolver;
import cc.dreamcode.platform.bukkit.serializer.ItemMetaSerializer;
import cc.dreamcode.platform.component.ComponentService;
import cc.dreamcode.platform.other.component.DreamCommandExtension;

import cc.dreamcode.rvfaxeventy.event.EventManager;
import cc.dreamcode.rvfaxeventy.scheduler.EventScheduler;
import cc.dreamcode.rvfaxeventy.command.EventCommand;
import cc.dreamcode.rvfaxeventy.command.handler.InvalidInputHandlerImpl;
import cc.dreamcode.rvfaxeventy.command.handler.InvalidPermissionHandlerImpl;
import cc.dreamcode.rvfaxeventy.command.handler.InvalidSenderHandlerImpl;
import cc.dreamcode.rvfaxeventy.command.handler.InvalidUsageHandlerImpl;
import cc.dreamcode.rvfaxeventy.command.result.BukkitNoticeResolver;
import cc.dreamcode.rvfaxeventy.config.MessageConfig;
import cc.dreamcode.rvfaxeventy.config.PluginConfig;
import cc.dreamcode.template.nms.api.VersionProvider;

import cc.dreamcode.utilities.adventure.AdventureProcessor;
import cc.dreamcode.utilities.adventure.AdventureUtil;
import cc.dreamcode.utilities.bukkit.StringColorUtil;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.commons.serializer.InstantSerializer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import eu.okaeri.configs.yaml.bukkit.serdes.itemstack.ItemStackFailsafe;
import eu.okaeri.configs.yaml.bukkit.serdes.serializer.ItemStackSerializer;
import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;

public final class RvfaxEventyPlugin extends DreamBukkitPlatform implements DreamBukkitConfig {

    @Getter
    private static RvfaxEventyPlugin instance;

    @Override
    public void load(@NonNull ComponentService componentService) {
        instance = this;

        AdventureUtil.setRgbSupport(true);
        StringColorUtil.setColorProcessor(new AdventureProcessor());
    }

    @Override
    public void enable(@NonNull ComponentService componentService) {
        componentService.setDebug(false);

        this.registerInjectable(BukkitTasker.newPool(this));
        this.registerInjectable(BukkitMenuProvider.create(this));

        this.registerInjectable(BukkitCommandProvider.create(this));
        componentService.registerExtension(DreamCommandExtension.class);

        this.registerInjectable(VersionProvider.getVersionAccessor());

        componentService.registerResolver(ConfigurationResolver.class);
        componentService.registerComponent(MessageConfig.class);

        componentService.registerComponent(BukkitNoticeResolver.class);
        componentService.registerComponent(InvalidInputHandlerImpl.class);
        componentService.registerComponent(InvalidPermissionHandlerImpl.class);
        componentService.registerComponent(InvalidSenderHandlerImpl.class);
        componentService.registerComponent(InvalidUsageHandlerImpl.class);

        componentService.registerComponent(PluginConfig.class, pluginConfig -> {
            // enable additional logs and debug messages
            componentService.setDebug(pluginConfig.debug);
        });

        componentService.registerComponent(EventManager.class);
        componentService.registerComponent(EventScheduler.class, EventScheduler::start);
        //

        componentService.registerComponent(EventCommand.class);
    }

    @Override
    public void disable() {
        // features need to be call when server is stopping
    }

    @Override
    public @NonNull DreamVersion getDreamVersion() {
        return DreamVersion.create("Rvfax-events", "1.0-InDEV", "Rvfax");
    }

    @Override
    public @NonNull OkaeriSerdesPack getConfigSerdesPack() {
        return registry -> {
            registry.register(new BukkitNoticeSerializer());
            registry.register(new MenuBuilderSerializer());

            registry.registerExclusive(ItemStack.class, new ItemStackSerializer(ItemStackFailsafe.BUKKIT));
            registry.registerExclusive(ItemMeta.class, new ItemMetaSerializer());
            registry.registerExclusive(Instant.class, new InstantSerializer(false));
        };
    }

}
