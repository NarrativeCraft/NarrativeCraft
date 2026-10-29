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

package fr.loudo.narrativecraft.studio.assets;

import fr.loudo.narrativecraft.api.util.Icons;
import fr.loudo.narrativecraft.client.studio.assets.TreeIconFileRegistryImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tree {

    private final String name;
    private final boolean directory;
    private final List<Tree> child = new ArrayList<>();
    private Tree parent;

    public Tree(String name, boolean directory) {
        this.name = name;
        this.directory = directory;
    }

    public void addChild(Tree tree) {
        if (!directory) {
            throw new IllegalStateException("Cannot add a child to file " + getPath());
        }
        for (Tree ancestor = this; ancestor != null; ancestor = ancestor.parent) {
            if (ancestor == tree) {
                throw new IllegalArgumentException("Cannot move " + tree.getPath() + " inside itself");
            }
        }
        if (tree.parent != null) {
            tree.parent.removeChild(tree);
        }
        child.add(tree);
        tree.parent = this;
    }

    public boolean removeChild(Tree tree) {
        if (!child.remove(tree)) {
            return false;
        }
        tree.parent = null;
        return true;
    }

    public boolean removeChild(String name) {
        for (Tree tree : child) {
            if (tree.name.equals(name)) {
                return removeChild(tree);
            }
        }
        return false;
    }

    public String getPath() {
        if (parent == null) {
            return "";
        }
        String parentPath = parent.getPath();
        return parentPath.isEmpty() ? name : parentPath + "/" + name;
    }

    public String getName() {
        return name;
    }

    public String resolveName() {
        String icon = null;
        if (directory) {
            icon = Icons.FOLDER;
        } else {
            int dot = name.lastIndexOf('.');
            if (dot != -1) {
                icon = TreeIconFileRegistryImpl.getInstance().getIcon(name.substring(dot));
            }
        }
        return (icon != null ? icon : Icons.FILE) + " " + name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public Tree getParent() {
        return parent;
    }

    public List<Tree> getChild() {
        return Collections.unmodifiableList(child);
    }
}
