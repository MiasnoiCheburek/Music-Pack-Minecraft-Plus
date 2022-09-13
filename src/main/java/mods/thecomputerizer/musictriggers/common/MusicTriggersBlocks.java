package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.MusicRecorder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MusicTriggersBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MusicTriggers.MODID);
    public static final RegistryObject<Block> MUSIC_RECORDER = BLOCKS.register("music_recorder", () -> new MusicRecorder(BlockBehaviour.Properties.of(Material.WOOD).strength(2F).sound(SoundType.WOOD)));

    public static void registerBlocks(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
