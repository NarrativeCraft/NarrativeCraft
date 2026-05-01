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

package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.compiler.Compiler;
import com.bladecoder.ink.compiler.IFileHandler;
import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.InkFileGenerator;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagDispatcherImpl;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandlerException;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.Translation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StoryCompilerHandler {

    public static Story compile() throws IOException {
        File mainInkFile = InkFileGenerator.getMainFile();
        String mainContent = Files.readString(mainInkFile.toPath());
        Compiler.Options options = new Compiler.Options();
        options.fileHandler = new FileHandler(mainInkFile.getParentFile());
        return new Compiler(mainContent, options).compile();
    }

    private static final Pattern TAG_PATTERN = Pattern.compile("#([^#\\n]+)");

    public static List<TagError> validateTags() {
        List<TagError> errors = new ArrayList<>();
        InkTagDispatcherImpl dispatcher = NarrativeCraftMod.getInstance().getInkTagDispatcher();

        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            File chapterDir = new File(NarrativeCraftFileUtil.getChaptersFolder(), chapter.toFileName());
            File chapterInkFile = new File(
                    chapterDir,
                    "chapter_" + chapter.getChapterIndex() + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE);
            validateInkFile(chapterInkFile, chapter, null, dispatcher, errors);

            for (Scene scene : chapter.getSceneManager().getList()) {
                File sceneInkFile = new File(
                        NarrativeCraftFileUtil.getSceneFolder(scene),
                        scene.getName().toLowerCase(Locale.ROOT).replace(' ', '_')
                                + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE);
                validateInkFile(sceneInkFile, chapter, scene, dispatcher, errors);
            }
        }

        return errors;
    }

    private static void validateInkFile(
            File inkFile,
            Chapter chapter,
            @Nullable Scene scene,
            InkTagDispatcherImpl dispatcher,
            List<TagError> errors) {
        if (!inkFile.exists()) return;

        String content;
        try {
            content = Files.readString(inkFile.toPath());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to read ink file for tag validation: {}", inkFile, e);
            return;
        }

        String[] lines = content.split("\n", -1);
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            if (line.trim().startsWith("//")) continue;

            Matcher matcher = TAG_PATTERN.matcher(line);
            while (matcher.find()) {
                String rawTag = matcher.group(1).trim();
                try {
                    dispatcher.dispatch(rawTag, scene);
                } catch (InkTagHandlerException e) {
                    errors.add(new TagError(chapter, scene, inkFile.getName(), lineIndex + 1, rawTag, e.getMessage()));
                }
            }
        }
    }

    static class FileHandler implements IFileHandler {

        private final File sourceRoot;

        public FileHandler(File sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        @Override
        public String resolveInkFilename(String includeName) {
            return new File(sourceRoot, includeName).getAbsolutePath();
        }

        @Override
        public String loadInkFileContents(String fullFilename) throws IOException {
            return Files.readString(new File(fullFilename).toPath());
        }
    }

    public record TagError(
            Chapter chapter, @Nullable Scene scene, String inkFileName, int line, String rawTag, String message) {

        public Component toMessage() {
            MutableComponent result = Component.empty().append("\n");

            result = result.copy()
                    .append(Translation.message("chapter")
                            .append(": ")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(ChatFormatting.BOLD))
                    .append(Component.literal(String.valueOf(chapter.getChapterIndex()))
                            .withStyle(ChatFormatting.RED)
                            .withStyle(style -> style.withBold(false)));

            if (scene == null) {
                result = result.copy()
                        .append(Component.literal(" (" + inkFileName + ")")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style.withBold(false)));
            }

            result = result.copy().append("\n");

            if (scene != null) {
                result = result.copy()
                        .append(Translation.message("scene")
                                .append(": ")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(ChatFormatting.BOLD))
                        .append(Component.literal(scene.getName() + " ")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(style -> style.withBold(false)))
                        .append(Component.literal("(" + inkFileName + ")")
                                .withStyle(ChatFormatting.GRAY)
                                .withStyle(style -> style.withBold(false)))
                        .append("\n");
            }

            result = result.copy()
                    .append(Translation.message("line").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" " + line + ": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(rawTag)
                            .withStyle(ChatFormatting.GRAY)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n")
                    .append(Component.literal("'" + message + "'")
                            .withStyle(ChatFormatting.RED)
                            .withStyle(style -> style.withBold(false)))
                    .append("\n");

            return result;
        }
    }
}
