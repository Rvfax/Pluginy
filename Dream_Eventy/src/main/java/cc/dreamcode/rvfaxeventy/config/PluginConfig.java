package cc.dreamcode.rvfaxeventy.config;

import cc.dreamcode.platform.bukkit.component.configuration.Configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Header;

@Configuration(child = "config.yml")
@Header("## Dream-Template (Main-Config) ##")
public class PluginConfig extends OkaeriConfig {

    @Comment
    @Comment("Debug pokazuje dodatkowe informacje do konsoli. Lepiej wylaczyc. :P")
    @Comment("Debug mode")
    @CustomKey("debug")
    public boolean debug = true;

    @Comment("Time between events (in seconds)")
    public long eventTime = 300L;

}
