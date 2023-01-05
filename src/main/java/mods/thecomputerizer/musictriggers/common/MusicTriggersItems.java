package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.CustomRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class MusicTriggersItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);
    public static final RegistryObject<Item> MUSIC_TRIGGERS_RECORD = ITEMS.register("music_triggers_record",
            () -> new MusicTriggersRecord(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CUSTOM_RECORD = ITEMS.register("custom_record",
            () -> new CustomRecord(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BLANK_RECORD = ITEMS.register("blank_record",
            () -> new BlankRecord(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MUSIC_RECORDER = ITEMS.register("music_recorder",
            () -> new BlockItem(MusicTriggersBlocks.MUSIC_RECORDER.get(), new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)));

    public static void registerItems(IEventBus bus) {
        ITEMS.register(bus);
    }
}
