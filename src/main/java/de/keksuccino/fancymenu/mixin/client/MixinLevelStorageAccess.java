package de.keksuccino.fancymenu.mixin.client;

import de.keksuccino.fancymenu.menu.world.LastWorldHandler;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class MixinLevelStorageAccess {

    private static final Logger MIXIN_LOGGER = LogManager.getLogger("fancymenu/mixin/LevelStorageAccess");

    @Shadow @Final LevelStorageSource.LevelDirectory levelDirectory;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(LevelStorageSource p_289971_, String p_289967_, Path p_289988_, CallbackInfo info) {
        LastWorldHandler.setLastWorld(this.levelDirectory.path().toFile().getPath().replace("\\", "/"), false);
    }

}
