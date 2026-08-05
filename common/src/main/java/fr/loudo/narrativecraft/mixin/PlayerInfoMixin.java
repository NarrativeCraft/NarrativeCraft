/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.character.MainCharacterAttribute;
import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = PlayerInfo.class, priority = 500)
public abstract class PlayerInfoMixin {

    @Shadow
    public abstract GameProfile getProfile();

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getCharacterSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        GameProfile profile = getProfile();

        UUID characterId = narrativecraft$resolveCharacterId(profile);
        List<ICharacterStory> charactersInWorld =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getCharactersInWorld();
        for (ICharacterStory characterStory : charactersInWorld) {
            if (!characterId.equals(characterStory.getId())) continue;

            if (narrativecraft$isMainCharacterWithPlayerSkin(characterStory)) {
                cir.setReturnValue(localPlayer.getSkin());
                continue;
            }

            narrativecraft$buildCharacterSkin(characterStory).ifPresent(cir::setReturnValue);
        }

        if (profile.id().equals(localPlayer.getUUID())) {
            narrativecraft$resolveLocalPlayerSkin().ifPresent(cir::setReturnValue);
        }
    }

    @Inject(method = "getProfile", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getProfile(CallbackInfoReturnable<GameProfile> cir) {
        CharacterStory mainCharacter =
                ClientNarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();
        if (mainCharacter == null) return;

        GameProfile gameProfile = cir.getReturnValue();
        UUID characterId = narrativecraft$resolveCharacterId(gameProfile);

        List<ICharacterStory> charactersInWorld =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getCharactersInWorld();
        for (ICharacterStory characterStory : charactersInWorld) {
            if (!characterId.equals(mainCharacter.getId())) continue;
            if (!gameProfile.name().equalsIgnoreCase(CharacterStory.USERNAME_VARIABLE)) continue;

            GameProfile replaced = new GameProfile(
                    gameProfile.id(), Minecraft.getInstance().player.getName().getString(), gameProfile.properties());
            cir.setReturnValue(replaced);
            return;
        }
    }

    private boolean narrativecraft$isMainCharacterWithPlayerSkin(ICharacterStory characterStory) {
        if (!(characterStory instanceof CharacterStory concreteCharacter)) return false;

        MainCharacterAttribute attr = concreteCharacter.getMainCharacterAttribute();
        return attr.isMainCharacter() && attr.getSkin() == MainCharacterAttribute.SkinMode.SKIN_OF_PLAYER;
    }

    private Optional<PlayerSkin> narrativecraft$buildCharacterSkin(ICharacterStory characterStory) {
        Identifier skinPath = narrativecraft$getSkinIdentifier(characterStory.getId());

        if (!narrativecraft$isDynamicTextureLoaded(skinPath)) return Optional.empty();

        return Optional.of(PlayerSkin.insecure(
                new ClientAsset.ResourceTexture(skinPath, skinPath), null, null, characterStory.getModelType()));
    }

    private Optional<PlayerSkin> narrativecraft$resolveLocalPlayerSkin() {
        CharacterStory mainCharacter =
                ClientNarrativeCraftMod.getInstance().getCharacterManager().getMainCharacter();

        if (mainCharacter == null
                || !ClientNarrativeCraftMod.getInstance().getPlayerSession().isInStory()) return Optional.empty();

        MainCharacterAttribute attr = mainCharacter.getMainCharacterAttribute();
        if (attr.getSkin() != MainCharacterAttribute.SkinMode.CLIENT_HAS_CHARACTER_SKIN) return Optional.empty();

        return narrativecraft$buildCharacterSkin(mainCharacter);
    }

    private UUID narrativecraft$resolveCharacterId(GameProfile profile) {
        for (Property property : profile.properties().get(FakePlayer.CHARACTER_ID_PROPERTY)) {
            try {
                return UUID.fromString(property.value());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return profile.id();
    }

    private Identifier narrativecraft$getSkinIdentifier(UUID characterId) {
        return Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "character/" + characterId);
    }

    private boolean narrativecraft$isDynamicTextureLoaded(Identifier skinPath) {
        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(skinPath);
        return texture instanceof DynamicTexture;
    }
}
