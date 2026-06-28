 # NarrativeCraft
<div align="center">
    Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
</div>
&nbsp;&nbsp;&nbsp;
<p align="center">
  <a href="https://discord.com/invite/E3zzNv79DN">
    <img width="250" height="150" alt="join_disord" src="https://github.com/user-attachments/assets/075953b6-de64-4f55-a9c8-fb407e50458b" />
  </a>
   &nbsp;&nbsp;&nbsp;
  <a href="https://narrativecraft.loudo.dev/">
    <img width="250" height="150" alt="documentation" src="https://github.com/user-attachments/assets/7eaaca5f-52ef-4a5f-bb2d-9320ffc247b5" />
  </a>
</p>

<hr />

**NarrativeCraft** is a Minecraft mod to create your own narrative games in Minecraft. If you don't want to learn a game engine to create stories with branching patterns, this mod does all the work for you.

## How does this work

NarrativeCraft works thanks to the amazing open source project [Ink](https://www.inklestudios.com/ink/), a narrative scripting language to write dialogs and create branching patterns. Text first, logic later.

## Features

NarrativeCraft is an all-in-one system, meaning that you have everything by default. 

Compatibility with other mods requires installing addons.

- Cutscenes
- Player recording
- Dialogs rendering (on screen or a character speaking)
- Camera angles for character dialog focus
- Interactions with trigger zones and clickable points
- Characters and NPCs
- Full management of your story
- Choices
- Saves
- Main screen (new game, continue...)
- Multiplayer

## About Multiplayer

NarrativeCraft is singleplayer first, meaning that it is mainly developed for singleplayer. However, multiplayer is supported, but it does not mean **co-op**, it means that you can play a story, show cutscenes, display characters or NPCs to a single player, so you can create more immersion for your server.

## Getting started

If you want to start learning **NarrativeCraft**, take a look at the [documentation](https://narrativecraft.loudo.dev); you'll be guided there to start your first story and learn the fundamentals of the mod.

## API

An API is available for use if you want to create comptability for other mod or if you want to take your story a step further.

There's a [documentation](https://narrativecraft.loudo.dev/api/getting-started) for the API.


### Gradle
```
maven {
    name "loudo"
    url "https://maven.loudo.dev"
}
```
```
compileOnly 'fr.loudo.narrativecraft:narrativecraft-api:2.0.11+mc{minecraft_version}'
```

### Maven
```
<repository>
    <id>loudo</id>
    <url>https://maven.loudo.dev</url>
</repository>
```
```
<dependency>
    <groupId>fr.loudo.narrativecraft</groupId>
    <artifactId>narrativecraft-api</artifactId>
    <version>2.0.11+mc{minecraft_version}</version>
</dependency>
```

Current minecraft versions available: `26.2` and `1.21.1`

## Contributing

Thank you for your interest in the project and for helping to make it better, take a look at [CONTRIBUTING.MD](CONTRIBUTING.MD) before making a pull request.

## Credits

- [ink](https://github.com/inkle/ink) - Scripting language
- [blade-ink-java](https://github.com/bladecoder/blade-ink-java) - Java adaptation of ink
