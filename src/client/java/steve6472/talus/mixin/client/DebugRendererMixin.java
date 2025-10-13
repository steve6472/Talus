package steve6472.talus.mixin.client;

import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import steve6472.talus.vaults.VaultsRenderer;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/13/2025
 * Project: Talus <br>
 */
@Mixin(DebugRenderer.class)
public class DebugRendererMixin
{
    @Shadow @Final private List<DebugRenderer.SimpleDebugRenderer> translucentRenderers;

    @Inject(at = @At("TAIL"), method = "refreshRendererList")
    public void refreshRendererList(CallbackInfo info)
    {
        this.translucentRenderers.add(new VaultsRenderer());
    }
}
