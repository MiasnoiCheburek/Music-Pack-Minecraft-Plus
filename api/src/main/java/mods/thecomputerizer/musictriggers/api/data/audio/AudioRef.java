package mods.thecomputerizer.musictriggers.api.data.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class AudioRef extends ParameterWrapper {

    private final String name;
    private final List<TriggerAPI> triggers;
    private InterruptHandler interruptHandler;

    public AudioRef(ChannelAPI channel, String name) {
        super(channel);
        this.name = name;
        this.triggers = new ArrayList<>();
    }

    public float getVolume() {
        return 0f;
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return AudioRef.class;
    }

    @Override
    protected String getTypeName() {
        return "Audio `"+getName()+"`";
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"chance",new ParameterInt(100));
        addParameter(map,"file_name",new ParameterString(""));
        addParameter(map,"pitch",new ParameterFloat(1f));
        addParameter(map,"play_once",new ParameterInt(0));
        addParameter(map,"play_x",new ParameterInt(1));
        addParameter(map,"resume_on_play",new ParameterBoolean(false));
        addParameter(map,"start_at",new ParameterInt(0));
        addParameter(map,"volume",new ParameterFloat(1f));
        initExtraParameters(map);
        return map;
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {

    }

    public boolean isInterrputedBy(@Nullable TriggerAPI trigger) {
        return Objects.isNull(this.interruptHandler) || this.interruptHandler.isInterrputedBy(trigger);
    }

    @Override
    public boolean isResource() {
        return false;
    }

    public void loadLocal(String location) {}

    public void loadRemote(String location) {}

    public boolean matchingTriggers(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAll(this.triggers,triggers);
    }

    public boolean parse(Table table) {
        List<String> triggerRefs = table.getValOrDefault("triggers",new ArrayList<>());
        if(!TriggerHelper.findTriggers(getChannel(),this.triggers,triggerRefs)) {
            logError("Failed to parse {}!",getTypeName());
            return false;
        }
        if(table.hasTable("must_finish"))
            this.interruptHandler = new InterruptHandler(this,table.getTableByName("must_finish"));
        return parseParameters(table);
    }

    /**
     * fade<0 = fade in
     * fade>0 = fade out
     */
    public void setFade(int fade) {}

    public void setItem(AudioItem item) {}

    public void start(TriggerAPI trigger) {}

    @Override
    public String toString() {
        return getTypeName();
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }

    public static class InterruptHandler extends ChannelElement {

        private final int priority;
        private final List<TriggerAPI> triggers;

        public InterruptHandler(AudioRef parent, Table table) {
            super(parent.getChannel());
            this.priority = table.getValOrDefault("priority",
                    ChannelHelper.getDebugBool("REVERSE_PRIORITY") ? Integer.MAX_VALUE : Integer.MIN_VALUE);
            this.triggers = parseTriggers(parent,table.getValOrDefault("trigger_whitelist",new ArrayList<>()));
        }

        public boolean isInterrputedBy(@Nullable TriggerAPI trigger) {
            if(Objects.isNull(trigger)) return false;
            int priority = trigger.getParameterAsInt("priority");
            return this.triggers.isEmpty() || (ChannelHelper.getDebugBool("REVERSE_PRIORITY") ?
                    priority<=this.priority : priority>=this.priority) || trigger.isContained(this.triggers);
        }

        @Override
        public boolean isResource() {
            return false;
        }

        private List<TriggerAPI> parseTriggers(AudioRef ref, List<String> triggerRefs) {
            List<TriggerAPI> triggers = new ArrayList<>();
            if(!TriggerHelper.findTriggers(getChannel(),triggers,triggerRefs)) {
                logError("Failed to parse 1 or more triggers in must_finish table for `{}`!",ref.getTypeName());
                return Collections.emptyList();
            }
            return triggers;
        }
    }
}