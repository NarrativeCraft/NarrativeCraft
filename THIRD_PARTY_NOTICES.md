# Third-party notices

NarrativeCraft is licensed under the MIT License (see `LICENSE`). The files listed below are derived from
third-party work and remain subject to their original license terms in addition to the project license.

## fabric-mod-ImGui

- Source: https://github.com/Enaium/fabric-mod-ImGui
- Copyright 2026 Enaium
- License: Apache License, Version 2.0

Derived files:

- `common/src/main/java/fr/loudo/narrativecraft/client/imgui/ClientImGuiBlaze3DRenderer.java`
  (from `game/26.3/src/main/java/cn/enaium/fabric/imgui/blaze3d/ImGuiImplBlaze3D.java`)
- `common/src/main/java/fr/loudo/narrativecraft/client/imgui/ClientImGui.java`
  (from `game/26.3/src/main/java/cn/enaium/fabric/imgui/DefaultImGui.java` and
  `core/src/main/java/cn/enaium/fabric/imgui/ImGuiService.java`)
- `common/src/main/java/fr/loudo/narrativecraft/mixin/SDLEventHandlerMixin.java`
  (from `game/26.3/src/main/java/cn/enaium/fabric/imgui/mixin/SDLEventHandlerMixin.java`)
- `common/src/main/resources/assets/narrativecraft/shaders/core/imgui.vsh`
- `common/src/main/resources/assets/narrativecraft/shaders/core/imgui.fsh`
  (from `game/26.3/src/main/resources/assets/fabric-gui-imgui/shaders/core/`)

Modifications: renamed to the NarrativeCraft package and identifiers, removed ImPlot, multi-viewport support,
the OpenGL (`ImGuiImplGl3`) code path and the ImGui texture registry, kept only the Blaze3D/RenderPearl renderer
(Vulkan), replaced the SDL event redirect with an injection, adapted naming conventions.

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
