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
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    private final Map<EquipmentSlot, StoredItem> itemsBySlot = new EnumMap<>(EquipmentSlot.class);

    public ChangeItemAction(int tick) {
        super(tick);
    }

    public ChangeItemAction(int tick, LivingEntity entity) {
        super(tick);

        DynamicOps<Tag> ops = entity.registryAccess().createSerializationContext(NbtOps.INSTANCE);

        for (EquipmentSlot slot : SLOT_IDS.keySet()) {
            ItemStack stack = entity.getItemBySlot(slot).copy();
            itemsBySlot.put(slot, StoredItem.fromStack(stack, ops));
        }
    }

    @Override
    public boolean differs(AbstractAction other) {
        if (!(other instanceof ChangeItemAction that)) {
            return false;
        }

        for (EquipmentSlot slot : SLOT_IDS.keySet()) {
            ItemStack thisStack = this.getItemStackForComparison(slot);
            ItemStack thatStack = that.getItemStackForComparison(slot);

            if (!ItemStack.isSameItemSameComponents(thisStack, thatStack)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addInt(itemsBySlot.size());

        for (Map.Entry<EquipmentSlot, StoredItem> entry : itemsBySlot.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            StoredItem storedItem = entry.getValue();

            Integer slotId = SLOT_IDS.get(slot);
            if (slotId == null) {
                throw new IOException("Unsupported slot: " + slot);
            }

            writer.addInt(slotId);
            writer.addString(BuiltInRegistries.ITEM.getKey(storedItem.item).toString());

            if (storedItem.data != null) {
                writer.addInt(DataType.ID_AND_COMPONENTS.id);
                writer.addString(storedItem.data);
            } else {
                writer.addInt(DataType.ID_ONLY.id);
            }
        }
    }

    @Override
    public void read(Reader reader) throws IOException {
        itemsBySlot.clear();

        int size = reader.readInt();

        for (int i = 0; i < size; i++) {
            EquipmentSlot slot = SLOT_IDS.inverse().get(reader.readInt());
            if (slot == null) {
                throw new IOException("Unknown equipment slot id " + i + 1);
            }

            Identifier key = Identifier.parse(reader.readString());

            int typeId = reader.readInt();
            String data = null;
            if (typeId == DataType.ID_AND_COMPONENTS.id) {
                data = reader.readString();
            }

            Item item =
                    BuiltInRegistries.ITEM.getOptional(key).orElseThrow(() -> new IOException("Unknown item: " + key));

            itemsBySlot.put(slot, new StoredItem(item, data));
        }
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {
        if (!(context.getEntity() instanceof LivingEntity entity)) {
            return ActionResult.IGNORED;
        }

        DynamicOps<Tag> ops = entity.registryAccess().createSerializationContext(NbtOps.INSTANCE);

        for (Map.Entry<EquipmentSlot, StoredItem> entry : itemsBySlot.entrySet()) {
            EquipmentSlot slot = entry.getKey();
            StoredItem storedItem = entry.getValue();

            entity.setItemSlot(slot, storedItem.toItemStack(ops).copy());
        }

        ((LivingEntityAccessor) entity).callDetectEquipmentUpdates();

        return ActionResult.OK;
    }

    @Override
    public String getId() {
        return ID;
    }

    private ItemStack getItemStackForComparison(EquipmentSlot slot) {
        StoredItem storedItem = itemsBySlot.get(slot);
        if (storedItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = storedItem.toItemStack(NbtOps.INSTANCE);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private record StoredItem(Item item, String data) {

        public static StoredItem fromStack(ItemStack itemStack, DynamicOps<Tag> ops) {
                Item item = itemStack.getItem();
                String data = null;

                DataResult<Tag> result = ItemStack.CODEC.encodeStart(ops, itemStack);
                Tag tag = result.resultOrPartial(error -> {
                }).orElse(null);

                if (tag instanceof CompoundTag compound && compound.contains("components")) {
                    data = compound.get("components").toString();
                }

                return new StoredItem(item, data);
            }

            public ItemStack toItemStack(DynamicOps<Tag> ops) {
                if (item == Items.AIR && data == null) {
                    return ItemStack.EMPTY;
                }

                if (data == null) {
                    return new ItemStack(item);
                }

                CompoundTag tag = createTag();
                if (tag == null) {
                    return ItemStack.EMPTY;
                }

                try {
                    return ItemStack.CODEC.parse(ops, tag).getOrThrow();
                } catch (Exception e) {
                    return ItemStack.EMPTY;
                }
            }

            private CompoundTag createTag() {
                CompoundTag tag = new CompoundTag();

                try {
                    tag.put("components", Utils.nbtFromString(data));
                } catch (CommandSyntaxException e) {
                    return null;
                }

                tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
                tag.put("count", IntTag.valueOf(1));
                return tag;
            }
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
