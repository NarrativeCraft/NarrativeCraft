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

import fr.loudo.narrativecraft.api.recording.action.IActionRegistry;

public class ActionRegister {

    public static void register(IActionRegistry registry) {
        registry.register(MovementAction.ID, MovementAction::new);
        registry.register(PoseAction.ID, PoseAction::new);
        registry.register(EntityByteAction.ID, EntityByteAction::new);
        registry.register(LivingEntityByteAction.ID, LivingEntityByteAction::new);
        registry.register(PlaceBlockAction.ID, PlaceBlockAction::new);
        registry.register(SilentPlaceBlockAction.ID, SilentPlaceBlockAction::new);
        registry.register(BreakBlockAction.ID, BreakBlockAction::new);
        registry.register(SwingAction.ID, SwingAction::new);
        registry.register(ChangeItemAction.ID, ChangeItemAction::new);
        registry.register(SleepAction.ID, SleepAction::new);
        registry.register(DestroyBlockStageAction.ID, DestroyBlockStageAction::new);
        registry.register(RideEntityAction.ID, RideEntityAction::new);
        registry.register(StopRideEntityAction.ID, StopRideEntityAction::new);
        registry.register(RightClickBlockAction.ID, RightClickBlockAction::new);
        registry.register(ItemPickupAction.ID, ItemPickupAction::new);
        registry.register(CloseContainerAction.ID, CloseContainerAction::new);
        registry.register(DeathAction.ID, DeathAction::new);
        registry.register(SpawnEntityAction.ID, SpawnEntityAction::new);
        registry.register(HurtAction.ID, HurtAction::new);
        registry.register(HorseByteAction.ID, HorseByteAction::new);
        registry.register(BoatDataAction.ID, BoatDataAction::new);
        registry.register(SilentDeathAction.ID, SilentDeathAction::new);
        registry.register(UseItemAction.ID, UseItemAction::new);
        registry.register(UseItemOnBlockAction.ID, UseItemOnBlockAction::new);
        registry.register(GameModeAction.ID, GameModeAction::new);
    }
}
