/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

package fr.loudo.narrativecraft.registry;

import fr.loudo.narrativecraft.api.registry.FileActionRegistry;
import fr.loudo.narrativecraft.files.SceneFiles;
import fr.loudo.narrativecraft.network.S2COpenScene;
import fr.loudo.narrativecraft.platform.Services;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerPlayer;

public class FileActionRegistryImpl implements FileActionRegistry {

    private static final FileActionRegistryImpl INSTANCE = new FileActionRegistryImpl();

    private final Map<String, BiConsumer<ServerPlayer, String>> actions = new HashMap<>();

    public static void initialize() {
        INSTANCE.register(SceneFiles.FORMAT, (player, data) -> {
            Services.PACKET.sendToPlayer(player, new S2COpenScene(data));
        });
    }

    private FileActionRegistryImpl() {}

    @Override
    public synchronized void register(String extension, BiConsumer<ServerPlayer, String> action) {
        Objects.requireNonNull(extension, "extension");
        Objects.requireNonNull(action, "action");
        if (actions.putIfAbsent(extension.toLowerCase(), action) != null) {
            throw new IllegalArgumentException(
                    "An action is already registered for the extension " + extension.toLowerCase());
        }
    }

    /**
     * Returns the action registered for this extension, or {@code null} if there is none.
     */
    public synchronized BiConsumer<ServerPlayer, String> getAction(String extension) {
        return actions.get(extension.toLowerCase());
    }

    public static FileActionRegistryImpl getInstance() {
        return INSTANCE;
    }
}
