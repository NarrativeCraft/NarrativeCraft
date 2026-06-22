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

package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class DataBlockAction extends AbstractAction {

    protected BlockPos blockPos;
    protected BlockState blockState;

    public DataBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick);
        this.blockPos = blockPos;
        this.blockState = blockState;
    }

    public DataBlockAction(int tick) {
        super(tick);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.addBlockPos(blockPos);

        writer.addString(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
        writer.addInt(blockState.getProperties().size());

        for (Property<?> property : blockState.getProperties()) {
            writer.addString(property.getName());
            writer.addString(getValueName(blockState, property));
        }
    }

    @Override
    public void read(Reader reader) throws IOException {
        blockPos = reader.readBlockPos();

        String blockId = reader.readString();
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));

        BlockState state = block.defaultBlockState();

        int propertyCount = reader.readInt();
        for (int i = 0; i < propertyCount; i++) {
            String propertyName = reader.readString();
            String propertyValue = reader.readString();

            Property<?> property = block.getStateDefinition().getProperty(propertyName);
            if (property != null) {
                state = applyProperty(state, property, propertyValue);
            }
        }

        this.blockState = state;
    }

    protected <T extends Comparable<T>> String getValueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    protected <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String raw) {
        return property.getValue(raw).map(v -> state.setValue(property, v)).orElse(state);
    }
}
