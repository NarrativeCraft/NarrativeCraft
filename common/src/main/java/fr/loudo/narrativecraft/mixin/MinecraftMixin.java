/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.ClearScreen;
import fr.loudo.narrativecraft.client.screens.mainScreen.MainScreen;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Shadow
    public abstract boolean isSingleplayer();

    @Inject(method = "openChatScreen", at = @At(value = "HEAD"), cancellable = true)
    private void narrativecraft$openChat(String p_91327_, CallbackInfo ci) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (editorMaker == null || editorMaker.getEnvironment() != NarrativeEnvironment.DEVELOPMENT) return;

        ClearScreen screen = new ClearScreen();
        setScreen(screen);
        ci.cancel();
    }

    @Inject(method = "pauseGame", at = @At(value = "HEAD"))
    private void narrativecraft$pauseGame(boolean suppressPauseMenuIfWeReallyArePausing, CallbackInfo ci) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (!playerSession.isInStory()) return;
        if (!isSingleplayer()) return;

        setScreen(new MainScreen(true));
    }

    @Inject(method = "startAttack", at = @At(value = "HEAD"), cancellable = true)
    private void narrativecraft$spectate(CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession.isInStory() && playerSession.inCamera()) {
            cir.cancel();
        }
    }
}
