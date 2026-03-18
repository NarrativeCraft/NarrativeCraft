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

package fr.loudo.narrativecraft.recording.actions;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.utils.Utils;
import java.io.IOException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChangeItemAction extends AbstractAction {

    public static final String ID = "change_item";

    private static final BiMap<EquipmentSlot, Integer> SLOT_IDS = ImmutableBiMap.<EquipmentSlot, Integer>builder()
            .put(EquipmentSlot.MAINHAND, 1)
            .put(EquipmentSlot.OFFHAND, 2)
            .put(EquipmentSlot.FEET, 3)
            .put(EquipmentSlot.LEGS, 4)
            .put(EquipmentSlot.CHEST, 5)
            .put(EquipmentSlot.HEAD, 6)
            .put(EquipmentSlot.BODY, 7)
            .put(EquipmentSlot.SADDLE, 8)
            .build();

    private EquipmentSlot slot;
    private ItemStack itemStack;
    private DynamicOps<Tag> ops;

    public ChangeItemAction(int tick) {
        super(tick);
    }

    public ChangeItemAction(int tick, EquipmentSlot slot, LivingEntity entity) {
        super(tick);
        this.slot = slot;
        this.itemStack = entity.getItemBySlot(slot).copy();
        this.ops = entity.registryAccess().createSerializationContext(NbtOps.INSTANCE);
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof ChangeItemAction that)) return false;

        return this.slot != that.slot || !ItemStack.isSameItemSameComponents(this.itemStack, that.itemStack);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(SLOT_IDS.get(slot));

        DataResult<Tag> result = ItemStack.CODEC.encodeStart(ops, itemStack);
        Tag tag = result.resultOrPartial(error -> {}).orElse(null);

        if (tag instanceof CompoundTag compound && compound.contains("components")) {
            writer.addInt(DataType.ID_AND_COMPONENTS.id);
            writer.addString(compound.toString());
        } else {
            writer.addInt(DataType.ID_ONLY.id);
            writer.addString(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
        }
    }

    @Override
    public void read(Reader reader) throws IOException {
        this.slot = SLOT_IDS.inverse().get(reader.readInt());
        int typeId = reader.readInt();
        String data = reader.readString();

        if (typeId == DataType.ID_AND_COMPONENTS.id) {
            try {
                CompoundTag nbt = Utils.nbtFromString(data);
                this.itemStack = ItemStack.CODEC
                        .parse(NbtOps.INSTANCE, nbt)
                        .resultOrPartial(e -> {})
                        .orElse(ItemStack.EMPTY);
            } catch (CommandSyntaxException e) {
                throw new IOException("Invalid NBT data", e);
            }
        } else {
            Identifier id = Identifier.parse(data);
            Item item =
                    BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new IOException("Unknown item: " + id));
            this.itemStack = new ItemStack(item);
        }
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {
        if (!(context.getEntity() instanceof LivingEntity entity)) {
            return ActionResult.IGNORED;
        }

        entity.setItemSlot(slot, itemStack.copy());
        ((LivingEntityAccessor) entity).callDetectEquipmentUpdates(); // Update for other players.

        return ActionResult.OK;
    }

    @Override
    public String getId() {
        return ID;
    }

    enum DataType {
        ID_ONLY(1),
        ID_AND_COMPONENTS(2);

        private final int id;

        DataType(int id) {
            this.id = id;
        }
    }
}
