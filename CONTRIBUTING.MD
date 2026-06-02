# Contributing

Thanks for showing interest! Here's what you need to know.

## Bug fix

If you find a bug, there are no requirements: fork the project, fix the bug and make a pull request. Bug fixes will always be welcome.

## Feature

### Issue exists

If an issue for a feature exists, you can work on it and then create a pull request.

### You have an idea

If you want to implement a feature and it's something small that does not change much in the codebase, you can do it on your side without creating an issue. However, it is recommended to first create an issue to keep track of what has been added.

If your feature requires a lot of work and you think it would be a great addition, please create an issue first so we can discuss it. If we think the feature is worth adding, you can start working on it, this way you won't waste your time.

## Creating a new Ink Action, Recording Action or Cutscene Layer

The project NarrativeCraft is **vanilla** first, meaning that if you have an idea for adding a new one of these three, don't do it for a **third-party mod** but only for a **vanilla** aspect of the game.

The reason is that I don't want to have everything centralized in one mod, but rather have separate mods that provide compatibility for other mods. This way it is:

- Less bloated
- Faster to update for recent Minecraft versions
- Less likely to create unexpected behavior
- Your own, you own the mod addon and get all the credit

If you want to create it for a compatibility aspect for a third-party mod, create your own mod by working with the API.

## Code

For any **client** aspect of the game, always add the prefix "Client" and place it in the `client` package, so that the client is kept separate from the server.

Before pushing, always run **Spotless** to clean the code.

`Gradle` -> `NarrativeCraft` -> `Tasks` -> `verification` -> `spotlessApply`