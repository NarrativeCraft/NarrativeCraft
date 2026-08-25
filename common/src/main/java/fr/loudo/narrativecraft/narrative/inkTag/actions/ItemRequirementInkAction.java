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

package fr.loudo.narrativecraft.narrative.inkTag.actions;

import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.inkAction.InkCommand;
import fr.loudo.narrativecraft.api.inkAction.Side;
import fr.loudo.narrativecraft.api.inkAction.syntax.ParsedCommand;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@InkCommand(
        keyword = "item_requirement",
        description =
                "Check if the player has the right amount of specified item, otherwise it will redirect to a stitch",
        side = Side.SERVER,
        syntax = "item_requirement <redirect_fail_stitch:string> <itemId:string> [amount:int=1] [customName:string]")
public class ItemRequirementInkAction extends InkAction {

    private Item item;
    private String redirect_fail_stitch;
    private String customName;
    private int amount;

    @Override
    protected InkActionResult doValidate(ParsedCommand cmd, IScene scene) {

        String itemId = cmd.getString("itemId");
        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);
        Optional<Item> item =
                itemLocation == null ? Optional.empty() : BuiltInRegistries.ITEM.getOptional(itemLocation);
        if (item.isEmpty()) {
            return InkActionResult.error("Item with id " + itemId + " is not registered.");
        }
        this.item = item.get();

        redirect_fail_stitch = cmd.getString("redirect_fail_stitch");
        amount = cmd.getInt("amount");
        customName = cmd.getString("customName");

        return InkActionResult.ok();
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        StoryHandler storyHandler = (StoryHandler) playerSession.getStoryHandler();
        if (storyHandler == null) return InkActionResult.ignored();

        ServerPlayer player = playerSession.getPlayer();

        int localAmount = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (sameItem(itemStack)) {
                localAmount += itemStack.getCount();
            }
        }

        if (localAmount < amount) {
            storyHandler.playStitch("on_" + redirect_fail_stitch + "_item_requireement_fail");
        }

        return InkActionResult.singleOk();
    }

    private boolean sameItem(ItemStack itemStack) {
        boolean itemNameMatch = customName.isEmpty();
        Component customNameItemComponent = itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
        if (customNameItemComponent != null && !customName.isEmpty()) {
            itemNameMatch = customNameItemComponent.getString().equalsIgnoreCase(customName);
        }
        return itemStack.is(item) && itemNameMatch;
    }
}
