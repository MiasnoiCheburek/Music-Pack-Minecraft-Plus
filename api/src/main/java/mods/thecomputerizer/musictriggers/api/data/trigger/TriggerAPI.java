package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.*;

@Getter
public abstract class TriggerAPI extends ParameterWrapper {

    private final Map<String,Timer> timers;
    private final Set<TriggerCombination> parents;
    private final String name;
    private ResourceContext resourceCtx;
    @Setter private State state;
    private int tracksPlayed;

    protected TriggerAPI(ChannelAPI channel, String name) {
        super(channel);
        this.timers = new HashMap<>();
        this.parents = new HashSet<>();
        this.name = name;
    }

    @Override
    public void activate() {
        this.state = State.ACTIVE;
        setTimer("ticks_before_audio",ACTIVE);
    }

    protected void addTimedParameter(Map<String,Parameter<?>> map, String name, State state, Parameter<?> parameter) {
        if(Objects.nonNull(parameter)) {
            addParameter(map,name,parameter);
            this.timers.put(name,new Timer(this,name,state));
        }
    }

    public boolean canActivate() {
        return this.state.activatable && !hasTime("active_cooldown") && !hasTime("ticks_before_active") &&
                hasNonEmptyAudioPool();
    }

    protected boolean canPersist() {
        return hasTime("persistence") &&
                (this.state==ACTIVE || (this.state==PLAYABLE && getParameterAsBoolean("passive_persistence")));
    }

    public boolean canPlayAudio() {
        if(this.tracksPlayed==0) return hasTime("ticks_before_audio");
        int maxTracks = getParameterAsInt("max_tracks");
        return maxTracks>0 && this.tracksPlayed<maxTracks && !hasTime("ticks_between_audio");
    }

    protected void clearTimers(State state) {
        consumeTimers(timer -> timer.clear(state));
    }

    protected void consumeTimers(Consumer<Timer> consumer) {
        this.timers.values().forEach(consumer);
    }

    @Override
    public void deactivate() {
        setState(this.channel.getSelector().isPlayable(this) ? PLAYABLE : IDLE);
        clearTimers(ACTIVE);
        setTimer("active_cooldown",PLAYABLE);
        this.tracksPlayed = 0;
    }

    public @Nullable AudioPool getAudioPool() {
        Collection<ChannelEventHandler> handlers = this.channel.getData().getTriggerEventMap().get(this);
        if(Objects.isNull(handlers)) return null;
        for(ChannelEventHandler handler : handlers)
            if(handler instanceof AudioPool) return (AudioPool)handler;
        return null;
    }

    public String getIdentifier() {
        String id = getParameterAsString("identifier");
        return Objects.nonNull(id) ? id : "not_set";
    }

    public String getNameWithID() {
        return getName();
    }

    public List<String> getRequiredMods() {
        return Collections.emptyList();
    }

    @Override
    public Class<? extends ChannelElement> getTypeClass() {
        return TriggerAPI.class;
    }

    @Override
    protected String getTypeName() {
        return "Trigger `"+getName()+"`";
    }

    public boolean hasNonEmptyAudioPool() {
        AudioPool pool = getAudioPool();
        return Objects.nonNull(pool) && pool.hasAudio();
    }

    public boolean hasTime(String name) {
        Timer timer = this.timers.get(name);
        return Objects.nonNull(timer) && timer.hasTime();
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"fade_in",new ParameterInt(0));
        addParameter(map,"fade_out",new ParameterInt(0));
        addParameter(map,"max_tracks",new ParameterInt(0));
        addParameter(map,"not",new ParameterBoolean(false));
        addParameter(map,"passive_persistence",new ParameterBoolean(false));
        addParameter(map,"priority",new ParameterInt(0));
        addParameter(map,"start_as_disabled",new ParameterBoolean(false));
        addParameter(map,"toggle_inactive_playable",new ParameterBoolean(false));
        addParameter(map,"toggle_save_status",new ParameterInt(0));
        addTimedParameter(map,"persistence",ACTIVE,new ParameterInt(0));
        addTimedParameter(map,"ticks_before_audio",ACTIVE,new ParameterInt(0));
        addTimedParameter(map,"ticks_between_audio",ACTIVE,new ParameterInt(0));
        addTimedParameter(map,"active_cooldown",PLAYABLE,new ParameterInt(0));
        addTimedParameter(map,"ticks_before_active",PLAYABLE,new ParameterInt(0));
        initExtraParameters(map);
        return map;
    }

    public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(triggers,this);
    }

    public boolean isDisabled() {
        return this.state==State.DISABLED;
    }

    public abstract boolean isPlayableContext(TriggerContextAPI<?,?> context);

    @Override
    public boolean isResource() {
        return false;
    }

    public boolean isServer() {
        return false;
    }

    public boolean matches(Collection<TriggerAPI> triggers) {
        return triggers.size()==1 && isContained(triggers);
    }

    public abstract boolean matches(TriggerAPI trigger);

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    public boolean parse(Table table) {
        if(parseParameters(table)) {
            successfullyParsed();
            return true;
        }
        return false;
    }

    @Override
    public void play() {
        this.tracksPlayed++;
    }

    @Override
    public void playable() {
        setState(PLAYABLE);
        setTimer("ticks_before_active",PLAYABLE);
    }

    /**
     * Queries the active state of the trigger & wraps isActive with additional checks
     */
    public boolean query(TriggerContextAPI<?,?> context) {
        if(isPlayableContext(context) || getParameterAsBoolean("not")) {
            setTimer("persistence",ACTIVE);
            return true;
        }
        return canPersist();
    }

    protected void setTimer(String name, State state) {
        Timer timer = this.timers.get(name);
        if(Objects.nonNull(timer)) timer.set(state);
    }

    protected void setTimers(State state) {
        consumeTimers(timer -> timer.set(state));
    }

    @Override
    public void stopped() {
        setTimer("ticks_between_audio",ACTIVE);
    }

    /**
     * Runs after this trigger has been successfully parsed
     */
    @SuppressWarnings("unchecked")
    protected void successfullyParsed() {
        if(hasParameter("resource_name")) {
            List<String> resourceName = (List<String>)getParameterAsList("resource_name");
            List<String> displayeName = (List<String>)getParameterAsList("display_name");
            String resourceMatcher = getParameterAsString("resource_matcher");
            String displayMatcher = getParameterAsString("display_matcher");
            this.resourceCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
        } else this.resourceCtx = null;
        setState(getParameterAsBoolean("start_as_disabled") ? DISABLED : IDLE);
    }

    @Override
    public void tickActive() {
        tickTimers(ACTIVE);
    }

    @Override
    public void tickPlayable() {
        tickTimers(PLAYABLE);
    }

    protected void tickTimers(State state) {
        consumeTimers(timer -> timer.tick(state));
    }

    @Override
    public void unplayable() {
        if(!isDisabled()) setState(State.IDLE);
        clearTimers(PLAYABLE);
    }

    @Getter
    public enum State {

        ACTIVE(false),
        DISABLED(false),
        IDLE(true),
        PLAYABLE(true);

        private final boolean activatable;

        State(boolean activatable) {
            this.activatable = activatable;
        }
    }

    protected static class Timer {

        private final TriggerAPI parent;
        private final String name;
        private final State state;
        private final MutableInt counter;

        protected Timer(TriggerAPI parent, String name, State state) {
            this.parent = parent;
            this.name = name;
            this.state = state;
            this.counter = new MutableInt();
        }

        protected void clear(State state) {
            if(this.state==state) this.counter.setValue(0);
        }

        protected boolean hasTime() {
            return this.counter.getValue()>0;
        }

        protected void set(State state) {
            if(this.state==state) this.counter.setValue(this.parent.getParameterAsInt(this.name));
        }

        protected void tick(State state) {
            if(this.state==state && this.counter.getValue()>0) this.counter.decrement();
        }
    }
}