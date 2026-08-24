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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import net.minecraft.network.FriendlyByteBuf;

public class CharacterStoryPayload extends NarrativeEntryPayload {

    private final String modelType;
    private final String entityTypeId;
    private final String customNbt;
    private final MainCharacterAttribute mainCharacterAttribute;
    private final String dialogDataJson;

    public CharacterStoryPayload(
            String name,
            String description,
            String modelType,
            String entityTypeId,
            String customNbt,
            MainCharacterAttribute mainCharacterAttribute,
            String dialogDataJson) {
        super(name, description);
        this.modelType = modelType != null ? modelType : "";
        this.entityTypeId = entityTypeId;
        this.customNbt = customNbt != null ? customNbt : "";
        this.mainCharacterAttribute =
                mainCharacterAttribute != null ? mainCharacterAttribute : new MainCharacterAttribute();
        this.dialogDataJson = dialogDataJson != null ? dialogDataJson : "{}";
    }

    public static CharacterStoryPayload read(FriendlyByteBuf buf) {
        return new CharacterStoryPayload(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                MainCharacterAttribute.read(buf),
                buf.readUtf());
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUtf(getName());
        buf.writeUtf(getDescription());
        buf.writeUtf(modelType);
        buf.writeUtf(entityTypeId);
        buf.writeUtf(customNbt);
        mainCharacterAttribute.write(buf);
        buf.writeUtf(dialogDataJson);
    }

    public String getModelType() {
        return modelType;
    }

    public String getEntityTypeId() {
        return entityTypeId;
    }

    public MainCharacterAttribute getMainCharacterAttribute() {
        return mainCharacterAttribute;
    }

    public String getDialogDataJson() {
        return dialogDataJson;
    }

    public String getCustomNbt() {
        return customNbt;
    }
}
