package fr.loudo.narrativecraft.api.editors.cutscene.keyframes;

public record KeyframeSegment<K extends Keyframe>(K from, K to, K p0, K p3, double rawT) {}
