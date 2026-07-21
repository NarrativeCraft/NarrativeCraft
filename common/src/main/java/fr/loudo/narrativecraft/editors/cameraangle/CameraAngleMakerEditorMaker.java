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

package fr.loudo.narrativecraft.editors.cameraangle;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleCharacterCaptured;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleEditorData;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.network.story.S2CCharacterStoryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.recording.actions.ChangeItemAction;
import fr.loudo.narrativecraft.recording.actions.EntityByteAction;
import fr.loudo.narrativecraft.recording.actions.MovementAction;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.syncher.SynchedEntityData;
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

    protected final List<CharacterPlacement> characterPlacements = new ArrayList<>();
    protected final Map<UUID, Entity> characterEntities = new HashMap<>();
    protected final List<TemplateReference> templateReferences = new ArrayList<>();
    protected final Map<UUID, List<UUID>> placementsByTemplateReference = new HashMap<>();
    protected final CameraAngle cameraAngle;
    protected final PlayerSession playerSession;
    protected final NarrativeEnvironment environment;

    public CameraAngleMakerEditorMaker(CameraAngle cameraAngle, PlayerSession playerSession) {
        this.cameraAngle = cameraAngle;
        this.playerSession = playerSession;
        this.environment = NarrativeEnvironment.DEVELOPMENT;
    }

    public CameraAngleMakerEditorMaker(
            CameraAngle cameraAngle, PlayerSession playerSession, NarrativeEnvironment environment) {
        this.cameraAngle = cameraAngle;
        this.playerSession = playerSession;
        this.environment = environment;
    }

    public void init() {
        if (cameraAngle.getScene() != null) {
            playerSession.apply(cameraAngle.getScene().getChapter(), cameraAngle.getScene());
        }
        playerSession.changeGameMode(GameType.SPECTATOR);
        for (CharacterPlacement characterPlacement : cameraAngle.getCharacterPlacements()) {
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(),
                    new S2CCharacterStoryAction(
                            characterPlacement.getCharacterStory().getId(), S2CCharacterStoryAction.Action.ADD));
            UtilsServer.sendCharacterSkin(playerSession.getPlayer(), characterPlacement.getCharacterStory());
            spawnEntity(characterPlacement);
            if (characterPlacement.isTemplate() && characterPlacement.getTemplateReferenceId() != null) {
                placementsByTemplateReference
                        .computeIfAbsent(characterPlacement.getTemplateReferenceId(), k -> new ArrayList<>())
                        .add(characterPlacement.getId());
            }
        }
        teleportToEditorOrigin();
        String dataJson = CameraAngleSerializer.serializeData(cameraAngle);
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CCameraAngleEditorData(dataJson));
    }

    public void teleportToEditorOrigin() {
        ServerPlayer player = playerSession.getPlayer();
        Vec3 position = null;
        if (!characterEntities.isEmpty()) {
            Entity entity = characterEntities.values().stream().findFirst().orElse(null);
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

    @Override
    public NarrativeEnvironment getEnvironment() {
        return NarrativeEnvironment.DEVELOPMENT;
    }

    public void tick() {
        hideEntitiesForOthers();
    }

    public void hideEntitiesForOthers() {
        for (ServerPlayer player : playerSession.getPlayer().level().players()) {
            if (player.getId() == playerSession.getPlayer().getId()) continue;
            characterEntities
                    .values()
                    .forEach(entity -> player.connection.send(new ClientboundRemoveEntitiesPacket(entity.getId())));
        }
    }

    public void stop() {
        reset();
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), BiStopEditorMaker.INSTANCE);
    }

    @Override
    public void reset() {
        playerSession.changeGameMode(playerSession.getLastGameType());
        if (environment == NarrativeEnvironment.DEVELOPMENT || cameraAngle.getScene() == null) {
            for (Entity entity : characterEntities.values()) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                if (entity instanceof FakePlayer player) {
                    playerSession
                            .getPlayer()
                            .connection
                            .send(new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID())));
                }
            }
        }
    }

    public void spawnEntity(CharacterPlacement characterPlacement) {
        if (characterPlacement.isTemplate() && environment != NarrativeEnvironment.DEVELOPMENT) return;
        UtilsServer.sendCharacterSkin(playerSession.getPlayer(), characterPlacement.getCharacterStory());
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CCharacterStoryAction(
                        characterPlacement.getCharacterStory().getId(), S2CCharacterStoryAction.Action.ADD));
        ICharacterStory characterStory = characterPlacement.getCharacterStory();
        ServerPlayer player = playerSession.getPlayer();
        ServerLevel level = player.level();
        Entity entity;
        if (characterStory.getEntityType() == EntityTypes.PLAYER) {
            entity = new FakePlayer(
                    level, FakePlayer.createCharacterProfile(characterStory, characterStory.getName()), true);
        } else {
            entity = characterStory.getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
        }
        entity.setPos(characterPlacement.getPosition());
        entity.setXRot((float) characterPlacement.getRotation().x);
        entity.setYRot((float) characterPlacement.getRotation().y);
        entity.setYHeadRot((float) characterPlacement.getRotation().y);
        entity.setOnGround(Utils.isOnGround(entity));

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
        characterEntities.put(characterPlacement.getId(), entity);
        Services.PACKET.sendToPlayer(
                player, new S2CCameraAnglePlacementEntitySpawned(characterPlacement.getId(), entity.getId()));
        setEntityPose(characterPlacement.getId(), characterPlacement.getPose());
    }

    public void spawnTemplateReference(TemplateReference reference) {
        ServerPlayer player = playerSession.getPlayer();
        List<UUID> createdIds = new ArrayList<>();
        for (Animation animation : resolveAnimations(reference)) {
            CharacterPlacement placement = createPlacementFromAnimation(animation, reference.id());
            if (placement == null) continue;
            spawnTemplateEntity(placement, animation);
            createdIds.add(placement.getId());
            String placementJson = CameraAngleSerializer.serializeSingleCharacterPlacement(placement);
            Services.PACKET.sendToPlayer(
                    player, new S2CCameraAngleCharacterCaptured(cameraAngle.getId(), placementJson));
            UtilsServer.sendCharacterSkin(player, placement.getCharacterStory());
            Services.PACKET.sendToPlayer(
                    player,
                    new S2CCharacterStoryAction(
                            placement.getCharacterStory().getId(), S2CCharacterStoryAction.Action.ADD));
        }
        placementsByTemplateReference.put(reference.id(), createdIds);
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

    private CharacterPlacement createPlacementFromAnimation(Animation animation, UUID templateReferenceId) {
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

        Vec3 position = lastMovement.getPosition();
        Vec3 rotation = new Vec3(lastMovement.getPitch(), lastMovement.getYaw(), 0.0);

        return new CharacterPlacement(
                UUID.randomUUID(), characterStory, position, rotation, new ArrayList<>(), true, templateReferenceId);
    }

    private void spawnTemplateEntity(CharacterPlacement placement, Animation animation) {
        if (!animation.initialize()) return;
        ICharacterStory characterStory = placement.getCharacterStory();

        RecordingData mainData = animation.getRecordingDataList().stream()
                .filter(data -> data.getRecordingId() == 0)
                .findFirst()
                .orElse(null);

        ServerPlayer player = playerSession.getPlayer();
        ServerLevel level = player.level();
        Entity entity;

        if (characterStory.getEntityType() == EntityTypes.PLAYER) {
            entity = new FakePlayer(
                    level, FakePlayer.createCharacterProfile(characterStory, characterStory.getName()), true);
        } else {
            entity = characterStory.getEntityType().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (entity == null) return;
            if (mainData != null) {
                CompoundTag initialNbt = mainData.getInitialNbt();
                if (initialNbt != null && !initialNbt.isEmpty()) {
                    entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), initialNbt));
                    entity.setUUID(UUID.randomUUID());
                }
            }
        }

        entity.setPos(placement.getPosition());
        entity.setXRot((float) placement.getRotation().x);
        entity.setYRot((float) placement.getRotation().y);
        entity.setYHeadRot((float) placement.getRotation().y);
        entity.setOnGround(Utils.isOnGround(entity));

        if (mainData != null) {
            IPlaybackContext context = new SingleEntityContext(entity);
            EntityByteAction lastEntityByte = findLastAction(mainData, EntityByteAction.class);
            ChangeItemAction lastItemChange = findLastAction(mainData, ChangeItemAction.class);
            if (lastEntityByte != null) lastEntityByte.execute(context, null);
            if (lastItemChange != null) lastItemChange.execute(context, null);
        }

        addEntityToWorld(entity, player, level, characterStory);
        entity.entityTags().add(ENTITY_TAG);
        characterPlacements.add(placement);
        characterEntities.put(placement.getId(), entity);
        Services.PACKET.sendToPlayer(
                player, new S2CCameraAnglePlacementEntitySpawned(placement.getId(), entity.getId()));
        setEntityPose(placement.getId(), placement.getPose());
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
            if (characterStory.getCharacterType() == CharacterType.NORMAL) {
                entity.setCustomNameVisible(true);
            }
            level.addFreshEntity(entity);
        }
    }

    public void teleportPlayerToTemplate(UUID refId) {
        TemplateReference reference = cameraAngle.getTemplateReferences().stream()
                .filter(ref -> ref.refId().equals(refId))
                .findFirst()
                .orElse(null);
        if (reference == null) return;
        List<UUID> placementIds = placementsByTemplateReference.get(reference.id());
        if (placementIds == null || placementIds.isEmpty()) return;
        for (UUID placementId : placementIds) {
            CharacterPlacement placement = characterPlacements.stream()
                    .filter(p -> p.getId().equals(placementId))
                    .findFirst()
                    .orElse(null);
            if (placement == null) continue;
            Vec3 position = placement.getPosition();
            ServerPlayer player = playerSession.getPlayer();
            player.connection.teleport(position.x, position.y, position.z, player.getYRot(), player.getXRot());
            return;
        }
    }

    public void removePlacement(UUID placementId) {
        Entity entity = characterEntities.remove(placementId);
        if (entity != null) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
        characterPlacements.removeIf(p -> p.getId().equals(placementId));
    }

    public void removeTemplateReference(UUID templateReferenceId) {
        List<UUID> placementIds = placementsByTemplateReference.remove(templateReferenceId);
        if (placementIds != null) {
            for (UUID placementId : new ArrayList<>(placementIds)) {
                removePlacement(placementId);
            }
        }
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(characterEntities.values());
    }

    public Entity getEntityForPlacement(UUID placementId) {
        return characterEntities.get(placementId);
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
        return new ArrayList<>(characterEntities.values());
    }

    public void setEntityPose(UUID placementId, Pose pose) {
        CharacterPlacement characterPlacement = characterPlacements.stream()
                .filter(p -> p.getId().equals(placementId))
                .findFirst()
                .orElse(null);
        if (characterPlacement == null) return;

        Entity entity = characterEntities.get(placementId);
        if (entity == null) return;

        entity.setPose(pose);
        SynchedEntityData entityData = entity.getEntityData();

        byte currentMask = entityData.get(EntityAccessor.getDATA_SHARED_FLAGS_ID());
        byte newMask = pose == Pose.CROUCHING ? (byte) (currentMask | 0x02) : (byte) (currentMask & ~0x02);
        entityData.set(EntityAccessor.getDATA_SHARED_FLAGS_ID(), newMask);

        if (pose == Pose.SHOOTING && entity instanceof LivingEntity livingEntity) {
            byte currentLivingFlags = entityData.get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS());
            byte newLivingFlags = 0;
            if (currentLivingFlags == 0) {
                if (!livingEntity.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) newLivingFlags = 3;
                else if (!livingEntity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) newLivingFlags = 1;
            }
            entityData.set(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS(), newLivingFlags);
        }

        characterPlacement.setPose(pose);
    }

    public List<TemplateReference> getTemplateReferences() {
        return templateReferences;
    }

    private record SingleEntityContext(Entity getEntity) implements IPlaybackContext {

        @Override
        public int getRecordingId() {
            return 0;
        }

        @Override
        public void respawnEntity() {}
    }
}
