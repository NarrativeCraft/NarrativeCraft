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

package fr.loudo.narrativecraft.editors.cameraangle;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import fr.loudo.narrativecraft.narrative.cameraangle.TemplateReference;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.recording.actions.ChangeItemAction;
import fr.loudo.narrativecraft.recording.actions.EntityByteAction;
import fr.loudo.narrativecraft.recording.actions.MovementAction;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

public class CameraAngleMakerEditorMaker implements EditorMaker {

    public static final String ENTITY_TAG = "nc_camera_angle";

    private final List<CharacterPlacement> characterPlacements = new ArrayList<>();
    private final List<Entity> characterEntities = new ArrayList<>();
    private final List<TemplateReference> templateReferences = new ArrayList<>();
    private final Map<UUID, List<Entity>> entitiesByTemplateReference = new HashMap<>();
    private final CameraAngle cameraAngle;
    private final PlayerSession playerSession;

    public CameraAngleMakerEditorMaker(CameraAngle cameraAngle, PlayerSession playerSession) {
        this.cameraAngle = cameraAngle;
        this.playerSession = playerSession;
    }

    public void init() {
        playerSession.changeGameMode(GameType.SPECTATOR);
        for (CharacterPlacement characterPlacement : cameraAngle.getCharacterPlacements()) {
            spawnEntity(characterPlacement);
        }
        for (TemplateReference reference : cameraAngle.getTemplateReferences()) {
            spawnTemplateReference(reference);
        }
        teleportToEditorOrigin();
    }

    public void teleportToEditorOrigin() {
        ServerPlayer player = playerSession.getPlayer();
        Vec3 position = null;
        if (!characterEntities.isEmpty()) {
            Entity entity = characterEntities.get(0);
            position = entity.position();
        } else {
            List<CameraView> cameraViews = cameraAngle.getCameras();
            if (!cameraViews.isEmpty()) {
                CameraView cameraView = cameraViews.get(0);
                position = cameraView.getPosition();
            }
        }
        if (position == null) return;
        player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
    }

    public void tick() {
        hideEntitiesForOthers();
    }

    public void hideEntitiesForOthers() {
        for (ServerPlayer player : playerSession.getPlayer().level().players()) {
            if (player.getId() == playerSession.getPlayer().getId()) continue;
            characterEntities.forEach(
                    entity -> player.connection.send(new ClientboundRemoveEntitiesPacket(entity.getId())));
            entitiesByTemplateReference
                    .values()
                    .forEach(list -> list.forEach(
                            entity -> player.connection.send(new ClientboundRemoveEntitiesPacket(entity.getId()))));
        }
    }

    public void stop() {
        playerSession.changeGameMode(playerSession.getLastGameType());
        for (Entity entity : characterEntities) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
        for (List<Entity> list : entitiesByTemplateReference.values()) {
            list.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    public void spawnEntity(CharacterPlacement characterPlacement) {
        ICharacterStory characterStory = characterPlacement.getCharacterStory();
        ServerPlayer player = playerSession.getPlayer();
        ServerLevel level = player.level();
        Entity entity;
        if (characterStory.getEntityType() == EntityType.PLAYER) {
            entity = new FakePlayer(level, new GameProfile(UUID.randomUUID(), characterStory.getName()), true);
        } else {
            entity = characterStory.getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
        }
        entity.setPos(characterPlacement.getPosition());
        entity.setXRot((float) characterPlacement.getRotation().x);
        entity.setYRot((float) characterPlacement.getRotation().y);
        entity.setYHeadRot((float) characterPlacement.getRotation().y);

        addEntityToWorld(entity, player, level, characterStory);

        if (entity instanceof LivingEntity livingEntity) {
            for (int i = 0; i < characterPlacement.getItems().size(); i++) {
                EquipmentSlot slot = EquipmentSlot.values()[i];
                ItemStack itemStack = characterPlacement.getItems().get(i);
                livingEntity.setItemSlot(slot, itemStack);
                ((LivingEntityAccessor) livingEntity).callDetectEquipmentUpdates();
            }
        }

        entity.entityTags().add(ENTITY_TAG);
        characterPlacements.add(characterPlacement);
        characterEntities.add(entity);
        Services.PACKET.sendToPlayer(
                player, new S2CCameraAnglePlacementEntitySpawned(characterPlacement.getId(), entity.getId()));
    }

    public void spawnTemplateReference(TemplateReference reference) {
        List<Animation> animations = resolveAnimations(reference);
        List<Entity> spawned = new ArrayList<>();
        for (Animation animation : animations) {
            Entity entity = spawnFromAnimation(animation);
            if (entity != null) spawned.add(entity);
        }
        entitiesByTemplateReference.put(reference.id(), spawned);
    }

    private List<Animation> resolveAnimations(TemplateReference reference) {
        Scene scene = cameraAngle.getScene();
        return switch (reference.sourceType()) {
            case ANIMATION -> {
                Animation animation = scene.getAnimationManager().getById(reference.refId());
                yield animation != null ? List.of(animation) : List.of();
            }
            case SUBSCENE -> {
                Subscene subscene = scene.getSubsceneManager().getById(reference.refId());
                yield subscene != null ? List.copyOf(subscene.getAnimations()) : List.of();
            }
            case CUTSCENE -> {
                Cutscene cutscene = scene.getCutsceneManager().getById(reference.refId());
                if (cutscene == null) yield List.of();
                List<Animation> all = new ArrayList<>(cutscene.getAnimations());
                for (Subscene subscene : cutscene.getSubscenes()) {
                    all.addAll(subscene.getAnimations());
                }
                yield all;
            }
        };
    }

    private Entity spawnFromAnimation(Animation animation) {
        if (!animation.initialize()) return null;
        ICharacterStory characterStory = animation.getCharacterStory();
        if (characterStory == null) return null;

        RecordingData mainData = animation.getRecordingDataList().stream()
                .filter(data -> data.getRecordingId() == 0)
                .findFirst()
                .orElse(null);
        if (mainData == null) return null;

        MovementAction lastMovement = findLastAction(mainData, MovementAction.class);
        if (lastMovement == null) return null;

        EntityByteAction lastEntityByte = findLastAction(mainData, EntityByteAction.class);
        ChangeItemAction lastItemChange = findLastAction(mainData, ChangeItemAction.class);

        ServerPlayer player = playerSession.getPlayer();
        ServerLevel level = player.level();
        Entity entity;

        if (characterStory.getEntityType() == EntityType.PLAYER) {
            entity = new FakePlayer(level, new GameProfile(UUID.randomUUID(), characterStory.getName()), true);
        } else {
            entity = characterStory.getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (entity == null) return null;
            CompoundTag initialNbt = mainData.getInitialNbt();
            if (initialNbt != null && !initialNbt.isEmpty()) {
                entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), initialNbt));
                entity.setUUID(UUID.randomUUID());
            }
        }

        IPlaybackContext context = new SingleEntityContext(entity);
        lastMovement.execute(context, null);
        if (lastEntityByte != null) lastEntityByte.execute(context, null);
        if (lastItemChange != null) lastItemChange.execute(context, null);

        addEntityToWorld(entity, player, level, characterStory);

        entity.entityTags().add(ENTITY_TAG);
        return entity;
    }

    private <T extends AbstractAction> T findLastAction(RecordingData data, Class<T> type) {
        T last = null;
        for (List<AbstractAction> actions : data.getActions().values()) {
            for (AbstractAction action : actions) {
                if (type.isInstance(action)) {
                    T candidate = type.cast(action);
                    if (last == null || candidate.getTick() > last.getTick()) {
                        last = candidate;
                    }
                }
            }
        }
        return last;
    }

    private void addEntityToWorld(
            Entity entity, ServerPlayer player, ServerLevel level, ICharacterStory characterStory) {
        if (entity instanceof FakePlayer fakePlayer) {
            player.connection.send(new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            level.addNewPlayer(fakePlayer);
        } else {
            entity.setInvulnerable(true);
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }
            entity.setCustomName(Component.literal(characterStory.getName()));
            entity.setCustomNameVisible(true);
            level.addFreshEntity(entity);
        }
    }

    public void teleportPlayerToTemplate(UUID refId) {
        TemplateReference reference = templateReferences.stream()
                .filter(ref -> ref.refId().equals(refId))
                .findFirst()
                .orElse(null);
        if (reference == null) return;
        List<Animation> animations = resolveAnimations(reference);
        for (Animation animation : animations) {
            if (!animation.initialize()) continue;
            RecordingData mainData = animation.getRecordingDataList().stream()
                    .filter(data -> data.getRecordingId() == 0)
                    .findFirst()
                    .orElse(null);
            if (mainData == null) continue;
            MovementAction lastMovement = findLastAction(mainData, MovementAction.class);
            if (lastMovement == null) continue;
            Vec3 position = lastMovement.getPosition();
            ServerPlayer player = playerSession.getPlayer();
            player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
            return;
        }
    }

    public void removePlacement(UUID placementId) {
        for (int i = 0; i < characterPlacements.size(); i++) {
            if (characterPlacements.get(i).getId().equals(placementId)) {
                characterEntities.get(i).remove(Entity.RemovalReason.DISCARDED);
                characterEntities.remove(i);
                characterPlacements.remove(i);
                cameraAngle.getCharacterPlacements().removeIf(p -> p.getId().equals(placementId));
                return;
            }
        }
    }

    public void removeTemplateReference(UUID templateReferenceId) {
        List<Entity> toRemove = entitiesByTemplateReference.remove(templateReferenceId);
        if (toRemove != null) {
            toRemove.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
            characterEntities.removeAll(toRemove);
        }
        cameraAngle.getTemplateReferences().removeIf(ref -> ref.id().equals(templateReferenceId));
    }

    public List<Entity> getEntities() {
        List<Entity> all = new ArrayList<>(characterEntities);
        entitiesByTemplateReference.values().forEach(all::addAll);
        return all;
    }

    public Entity getEntityForPlacement(UUID placementId) {
        for (int i = 0; i < characterPlacements.size(); i++) {
            if (characterPlacements.get(i).getId().equals(placementId)) {
                return characterEntities.get(i);
            }
        }
        return null;
    }

    public CameraAngle getCameraAngle() {
        return cameraAngle;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public List<CharacterPlacement> getCharacterPlacements() {
        return characterPlacements;
    }

    public List<Entity> getCharacterEntities() {
        return characterEntities;
    }

    public List<TemplateReference> getTemplateReferences() {
        return templateReferences;
    }

    private static final class SingleEntityContext implements IPlaybackContext {
        private final Entity entity;

        SingleEntityContext(Entity entity) {
            this.entity = entity;
        }

        @Override
        public Entity getEntity() {
            return entity;
        }

        @Override
        public int getRecordingId() {
            return 0;
        }

        @Override
        public void respawnEntity() {}
    }
}
