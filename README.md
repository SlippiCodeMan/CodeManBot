![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlippiCodeMan/CodeManBot/Java%20CI%20with%20Gradle)
![License](https://img.shields.io/github/license/SlippiCodeMan/CodeManBot)
![Discord API](https://img.shields.io/badge/discord%20api-JDA-%23843fd1)
![Codacy grade](https://img.shields.io/codacy/grade/7841a115752a446db5cff42a5243081b)
![GitHub issues](https://img.shields.io/github/issues/SlippiCodeMan/CodeManBot)

# CodeMan

[CodeMan](https://slippicodeman.github.io/CodeManWebsite/) is a Discord bot to manage Slippi connect codes for SSBM. 

## Installation

First make sure [gradle](https://gradle.org/) is installed on your system.

Then you can build it with:

```bash
gradle shadowjar
```

> If gradle is too old on your system you can use the `gradlew` script instead (or `gradle.bat` if you run Windows).

## Usage

To use it you have to create a `.env` file at the root of the project containing those env vars :
-   `CODEMAN_DB_URI` to a [mongodb](https://www.mongodb.com/) database access url.
-   `CODEMAN_PROD_TOKEN` to a discord bot token.
-   `CODEMAN_DEV_TOKEN` (optional) to a test bot token.
-   `CODEMAN_EXEC_MODE` to prod or dev

Then you can run it with:

```bash
gradle run
```

### Docker

To run it inside docker you need to install docker and then:

To create your container:
```bash
gradle docker
```

To start your container:
```bash
gradle dockerStart
```

To stop your container:
```bash
gradle dockerStop
```

> Use `sudo` before those commands if you get a permission error.

## Contributing

We only accept pull requests on the dev branch which has autodeployement on the dev bot.
When we feel everything is stable we merge dev into main which has autodeployement on the production bot.

If your change is huge please open an issue first.

## License
[MIT](https://choosealicense.com/licenses/mit/)