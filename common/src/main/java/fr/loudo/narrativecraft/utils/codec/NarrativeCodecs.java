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

package fr.loudo.narrativecraft.utils.codec;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.utils.Utils;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class NarrativeCodecs {

    public static final int FORMAT_VERSION = 1;

    public static final Codec<UUID> UUID_CODEC = UUIDUtil.STRING_CODEC;

    public static final MapCodec<UUID> ID = UUID_CODEC.fieldOf("id");

    public static final MapCodec<Vec3> POSITION = vec3("x", "y", "z");

    public static final MapCodec<Vec3> ROTATION = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.DOUBLE.fieldOf("xRot").forGetter(Vec3::x),
                    Codec.DOUBLE.fieldOf("yRot").forGetter(Vec3::y),
                    field(Codec.DOUBLE, "roll", 0.0).forGetter(Vec3::z))
            .apply(instance, Vec3::new));

    private static final MapCodec<Integer> VERSION = Codec.INT
            .optionalFieldOf("version")
            .xmap(version -> version.orElse(FORMAT_VERSION), Optional::of)
            .validate(version -> version <= FORMAT_VERSION
                    ? DataResult.success(version)
                    : DataResult.error(() -> "Written by a newer NarrativeCraft (format version " + version + ")"));

    private NarrativeCodecs() {}

    public static <A> MapCodec<A> field(Codec<A> codec, String name, A defaultValue) {
        return codec.lenientOptionalFieldOf(name).xmap(value -> value.orElse(defaultValue), Optional::ofNullable);
    }

    public static <A> MapCodec<A> fieldOrElseGet(Codec<A> codec, String name, Supplier<A> defaultValue) {
        return codec.lenientOptionalFieldOf(name).xmap(value -> value.orElseGet(defaultValue), Optional::ofNullable);
    }

    public static <A> MapCodec<Optional<A>> optionalField(Codec<A> codec, String name) {
        return codec.lenientOptionalFieldOf(name);
    }

    public static MapCodec<Vec3> vec3(String xKey, String yKey, String zKey) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.DOUBLE.fieldOf(xKey).forGetter(Vec3::x),
                        Codec.DOUBLE.fieldOf(yKey).forGetter(Vec3::y),
                        Codec.DOUBLE.fieldOf(zKey).forGetter(Vec3::z))
                .apply(instance, Vec3::new));
    }

    public static <E extends Enum<E>> Codec<E> enumByName(Class<E> enumClass) {
        return Codec.STRING.comapFlatMap(
                name -> {
                    try {
                        return DataResult.success(Enum.valueOf(enumClass, name.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown " + enumClass.getSimpleName() + " " + name);
                    }
                },
                Enum::name);
    }

    public static <A> Codec<List<A>> lenientList(Codec<A> elementCodec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getList(input).map(elements -> {
                    List<A> values = new ArrayList<>();
                    elements.accept(element -> {
                        DataResult<A> result = elementCodec.parse(ops, element);
                        result.error()
                                .ifPresent(error ->
                                        NarrativeCraftMod.LOGGER.warn("Skipped invalid element: {}", error.message()));
                        result.result().ifPresent(values::add);
                    });
                    return Pair.of(values, input);
                });
            }

            @Override
            public <T> DataResult<T> encode(List<A> input, DynamicOps<T> ops, T prefix) {
                return elementCodec.listOf().encode(input, ops, prefix);
            }
        };
    }

    public static <A> Codec<A> versioned(MapCodec<A> codec) {
        return Codec.mapPair(VERSION, codec)
                .xmap(Pair::getSecond, value -> Pair.of(FORMAT_VERSION, value))
                .codec();
    }

    public static <A> StreamCodec<ByteBuf, A> streamCodec(Codec<A> codec) {
        return ByteBufCodecs.fromCodec(codec);
    }

    public static DataResult<ItemStack> parseItemStack(String snbt) {
        try {
            CompoundTag tag = Utils.nbtFromString(snbt);
            return ItemStack.CODEC.parse(NbtOps.INSTANCE, tag);
        } catch (CommandSyntaxException exception) {
            return DataResult.error(() -> "Invalid item " + snbt + ": " + exception.getMessage());
        }
    }

    public static String writeItemStack(ItemStack itemStack) {
        return ItemStack.CODEC
                .encodeStart(NbtOps.INSTANCE, itemStack)
                .result()
                .map(Object::toString)
                .orElse("{}");
    }
}
