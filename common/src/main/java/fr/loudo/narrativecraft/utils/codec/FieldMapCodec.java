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

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class FieldMapCodec<T> extends MapCodec<T> {

    private final Supplier<T> factory;
    private final List<Field<T, ?>> fields;

    private FieldMapCodec(Supplier<T> factory, List<Field<T, ?>> fields) {
        this.factory = factory;
        this.fields = List.copyOf(fields);
    }

    public static <T> Builder<T> builder(Supplier<T> factory) {
        return new Builder<>(factory);
    }

    @Override
    public <O> Stream<O> keys(DynamicOps<O> ops) {
        return fields.stream().map(field -> ops.createString(field.name()));
    }

    @Override
    public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
        T value = factory.get();
        for (Field<T, ?> field : fields) {
            field.decodeInto(ops, input, value);
        }
        return DataResult.success(value);
    }

    @Override
    public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
        for (Field<T, ?> field : fields) {
            field.encodeInto(input, ops, prefix);
        }
        return prefix;
    }

    public record Field<T, V>(String name, Codec<V> codec, Function<T, V> getter, BiConsumer<T, V> setter) {

        private <O> void decodeInto(DynamicOps<O> ops, MapLike<O> input, T target) {
            O element = input.get(name);
            if (element == null) return;
            codec.parse(ops, element).result().ifPresent(value -> setter.accept(target, value));
        }

        private <O> void encodeInto(T source, DynamicOps<O> ops, RecordBuilder<O> builder) {
            V value = getter.apply(source);
            if (value == null) return;
            builder.add(name, codec.encodeStart(ops, value));
        }
    }

    public static final class Builder<T> {

        private final Supplier<T> factory;
        private final List<Field<T, ?>> fields = new ArrayList<>();

        private Builder(Supplier<T> factory) {
            this.factory = factory;
        }

        public <V> Builder<T> field(String name, Codec<V> codec, Function<T, V> getter, BiConsumer<T, V> setter) {
            fields.add(new Field<>(name, codec, getter, setter));
            return this;
        }

        public FieldMapCodec<T> build() {
            return new FieldMapCodec<>(factory, fields);
        }
    }
}
