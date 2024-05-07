package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml.TomlEntry;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlRemapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 Assumes 6.3.1 since V6 didn't have config versioning
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class MTConfigV6 extends ConfigVersion {
    
    public static final MTConfigV6 V6_3_1 = new MTConfigV6(3,1);
    
    protected MTConfigV6(int major, int minor) {
        super(6,major,minor);
    }
    
    protected MTConfigV6(int major, int minor, String qualifierName, int qualifierBuild) {
        super(6,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override public Toml getGlobal() {
        Toml global = Toml.getEmpty();
        Toml debug = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/debug",false,this);
        if(Objects.nonNull(debug)) {
            Toml registration = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/registration",false,this);
            if(Objects.nonNull(registration)) {
                TomlEntry<?> entry = registration.getEntry("CLIENT_SIDE_ONLY");
                if(Objects.nonNull(entry)) debug.addEntry(entry.getKey(),entry.getValue());
                entry = registration.getEntry("REGISTER_DISCS");
                if(Objects.nonNull(entry)) debug.addEntry(entry.getKey(),entry.getValue());
                FileHelper.get(MTRef.CONFIG_PATH+"/registration.toml",false).delete();
            }
            global.addTable("debug",debug);
            FileHelper.get(MTRef.CONFIG_PATH+"/debug.toml",false).delete();
        }
        Toml channels = ChannelHelper.openToml(MTRef.CONFIG_PATH+"/channels",false,this);
        if(Objects.nonNull(channels)) {
            global.addTable("channels",channels);
            FileHelper.get(MTRef.CONFIG_PATH+"/channels.toml",false).delete();
        }
        return global;
    }
    
    @Override public String getPathMain(Toml channel) {
        return MTRef.CONFIG_PATH+"/"+channel.getOrSetValue("main",channel.getName()+"/main"); //6.3.1 Didn't write the default parameters
    }
    
    @Override public @Nullable TomlRemapper getRemapper(@Nullable TableRef ref) { //TODO Make a ParameterRemapper class or something to consolidate stuff
        if(Objects.isNull(ref)) return null;
        String name = ref.getName();
        switch(name) {
            case "audio":
            case "universal_audio": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    entry = remapAudioEntry(entry);
                    Toml table = upgradeToTable(entry);
                    if(Objects.nonNull(table)) {
                        parent.addTable(entry.getKey(),table);
                        return null;
                    }
                    return entry;
                }
            };
            case "channel": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml toml, TomlEntry<?> entry) {
                    return remapChannelInfoEntry(toml.getName(),entry);
                }
            };
            case "channels": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper(MTDataRef.CHANNEL_INFO);
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return entry;
                }
            };
            case "global":
            case "main":
                return new ConfigRemapper(ref) {
                @Override public TomlRemapper getNextRemapper(TableRef next) {
                    return getRemapper(next);
                }
            };
            case "debug": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return null;
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return remapDebugEntry(entry);
                }
            };
            case "songs": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper("universal".equals(table) ? MTDataRef.UNIVERSAL_AUDIO : MTDataRef.AUDIO);
                }
                @Override public String remapTable(String name) {
                    return name;
                }
                @Override public TomlEntry<?> remapEntry(Toml toml, TomlEntry<?> entry) {
                    return entry;
                }
            };
            case "triggers": return new TomlRemapper() {
                @Nullable @Override public TomlRemapper getNextRemapper(String table) {
                    return getRemapper("universal".equals(table) ? MTDataRef.UNIVERSAL_TRIGGERS : ref.findChild(table));
                }
                @Override public String remapTable(String name) {
                    return remapTriggerName(name);
                }
                @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                    return entry;
                }
            };
            default: {
                if(name.equals("universal_triggers") || TriggerRegistry.isRegistred(name)) {
                    return new TomlRemapper() {
                        @Nullable @Override public TomlRemapper getNextRemapper(String name) {
                            return getRemapper(ref.findChild(name));
                        }
                        @Override public String remapTable(String name) {
                            return name;
                        }
                        @Override public TomlEntry<?> remapEntry(Toml parent, TomlEntry<?> entry) {
                            return remapTriggerEntry(parent.getName(),entry);
                        }
                    };
                }
                return null;
            }
        }
    }
    
    @Override public Toml getRenders(Toml channel) {
        String path = MTRef.CONFIG_PATH+"/"+channel.getOrSetValue("transitions",channel.getName()+"/transitions");
        Toml renders = ChannelHelper.openToml(path,false,this);
        if(Objects.nonNull(renders)) {
            File file = FileHelper.get(path+".toml",false);
            if(file.getName().contains("transitions"))
                file.renameTo(new File(file.getParent(),file.getName().replace("transitions","renders")));
        }
        return renders;
    }
    
    @Override public Toml getToggles(Toml global) {
        Toml toggles = Toml.getEmpty();
        Toml channels = global.getTable("channels");
        if(Objects.nonNull(channels)) {
            for(Toml channel : channels.getAllTables()) {
                String path = MTRef.CONFIG_PATH+"/"+channel.getOrSetValue("toggles",channel.getName()+"/toggles");
                Toml channelToggles = ChannelHelper.openToml(path,false,this);
                if(Objects.nonNull(channelToggles)) {
                    for(Toml toggle : channelToggles.getAllTables())
                        toggles.addTable(toggle.getName(),toggle);
                    FileHelper.get(path+".toml",false).delete(); //Remove the unused file
                }
            }
        }
        toggles.addComments(getHeaderLines("toggles"));
        MTDataRef.writeToFile(toggles,MTRef.CONFIG_PATH+"/"+global.getOrSetValue("toggles_path","toggles"));
        return toggles;
    }
    
    @Override
    public ConfigVersion getVersionTarget() {
        return ConfigVersionManager.findLatestQualified(7,0,0);
    }
    
    @Override public TomlEntry<?> remapAudioEntry(TomlEntry<?> entry) {
        return entry.getKey().equals("must_finish") ? new TomlEntry<>("interrupt_handler",entry.getValue()) : entry;
    }
    
    @Override public TomlEntry<?> remapChannelInfoEntry(String channel, TomlEntry<?> entry) {
        String key = entry.getKey();
        switch(key) {
            case "commands":
            case "jukebox":
            case "main":
            case "redirect":
            case "transitions": {
                TomlEntry<?> transformed = entry;
                key = "transitions".equals(key) ? "renders" : key;
                if(!key.equals(entry.getKey())) transformed = new TomlEntry<>(key,transformed.getValue());
                String path = entry.getValue().toString();
                path = path.startsWith(channel) ? path.substring(channel.length()+1) : path;
                path = path.replace("transitions","renders");
                if(!transformed.getValue().equals(path)) transformed = new TomlEntry<>(transformed.getKey(),path);
                return transformed;
            }
            case "explicit_overrides": return new TomlEntry<>("explicitly_overrides",entry.getValue());
            case "overrides_normal_music": return new TomlEntry<>("overrides_music",entry.getValue());
            case "pause_overrides": return new TomlEntry<>("pauses_overrides",entry.getValue());
            case "songs_folder": return new TomlEntry<>("local_folder",entry.getValue());
            case "toggles": return null;
            default: return entry;
        }
    }
    
    @Override public TomlEntry<?> remapDebugEntry(TomlEntry<?> entry) {
        switch(entry.getKey()) {
            case "ALLOW_TIMESTAMPS": return new TomlEntry<>("allow_timestamps",entry.getValue());
            case "BLOCK_STREAMING_ONLY": return new TomlEntry<>("block_sound_effects",entry.getValue());
            case "BLOCKED_MOD_CATEGORIES": return new TomlEntry<>("blocked_sound_categories",entry.getValue());
            case "CLIENT_SIDE_ONLY": return new TomlEntry<>("client_only",entry.getValue());
            case "COMBINE_EQUAL_PRIORITY": return new TomlEntry<>("independent_audio_pools",entry.getValue());
            case "CURRENT_SONG_ONLY": return new TomlEntry<>("show_song_info",entry.getValue());
            case "ENCODING_QUALITY": return new TomlEntry<>("encoding_quality",entry.getValue());
            case "INTERRUPTED_AUDIO_CATEGORIES": return new TomlEntry<>("interrupted_sound_categories",entry.getValue());
            case "LOG_LEVEL":
            case "MAX_HOVER_ELEMENTS": return null;
            case "PAUSE_WHEN_TABBED": return new TomlEntry<>("pause_unless_focused",entry.getValue());
            case "PLAY_NORMAL_MUSIC": return new TomlEntry<>("play_normal_music",entry.getValue());
            case "REGISTER_DISCS": return new TomlEntry<>("enable_discs",entry.getValue());
            case "RESAMPLING_QUALITY": return new TomlEntry<>("resampling_quality",entry.getValue());
            case "REVERSE_PRIORITY": return new TomlEntry<>("reverse_priority",entry.getValue());
            case "SHOW_DEBUG": return new TomlEntry<>("enable_debug_info",entry.getValue());
            default: return entry;
        }
    }
    
    @Override public TomlEntry<?> remapTriggerEntry(String name, TomlEntry<?> entry) {
        switch(entry.getKey()) {
            case "biome_category": return new TomlEntry<>("biome_tag",entry.getValue());
            case "check_higher_rainfall": return new TomlEntry<>("rainfall_greater_than",entry.getValue());
            case "check_lower_temp": return new TomlEntry<>("temperature_greater_than",entry.getValue());
            case "level": {
                switch(name) { //Double layer switch :|
                    case "lowhp": return new TomlEntry<>("health_percentage",entry.getValue());
                    case "raid": return new TomlEntry<>("wave",entry.getValue());
                    case "season": return new TomlEntry<>("season",entry.getValue());
                    default: return entry;
                }
            }
            case "song_delay": return new TomlEntry<>("ticks_between_audio",entry.getValue());
            case "start_delay": return new TomlEntry<>("ticks_before_active",entry.getValue());
            case "start_toggled": return new TomlEntry<>("start_as_disabled",entry.getValue());
            case "stop_delay": return new TomlEntry<>("active_cooldown",entry.getValue());
            case "trigger_delay": return new TomlEntry<>("ticks_before_audio",entry.getValue());
            default: return entry;
        }
    }
    
    @Override public String remapTriggerName(String name) {
        return name.equals("fallingstars") ? "starshower" : name;
    }
    
    @Nullable @Override public Toml upgradeToTable(TomlEntry<?> entry) {
        return entry.getKey().equals("interrupt_handler") ? Toml.getEmpty() : null;
    }
    
    @Override public void verifyJukebox(Toml channel) {
        String path = MTRef.CONFIG_PATH+"/"+channel.getOrSetValue("jukebox",channel.getName()+"/jukebox");
        List<String> oldLines = ChannelHelper.openTxt(path,this);
        oldLines.removeIf(str -> StringUtils.isEmpty(str) ||
                                 str.startsWith("Format this like name") ||
                                 str.startsWith("The key refers to a lang key") ||
                                 str.startsWith("determines the description of the") ||
                                 str.startsWith("Any lines with Format in the name") ||
                                 str.startsWith("Make sure each new entry is on a new line"));
        List<String> lines = new ArrayList<>();
        for(String headerLine : getHeaderLines("jukebox")) lines.add("#"+headerLine);
        lines.addAll(oldLines);
        FileHelper.writeLines(path+".txt",lines,false);
    }
    
    @Override public void verifyRedirct(Toml channel) {
        String path = MTRef.CONFIG_PATH+"/"+channel.getOrSetValue("redirect",channel.getName()+"/redirect");
        List<String> oldLines = ChannelHelper.openTxt(path,this);
        oldLines.removeIf(str -> StringUtils.isEmpty(str) ||
                              str.startsWith("Format this like name") ||
                              str.startsWith("If you are trying to redirect to an already") ||
                              str.startsWith("Any lines with Format in the name") ||
                              str.startsWith("Make sure each new entry is on a new line"));
        List<String> lines = new ArrayList<>();
        for(String headerLine : getHeaderLines("redirect")) lines.add("#"+headerLine);
        lines.addAll(oldLines);
        FileHelper.writeLines(path+".txt",lines,false);
    }
}