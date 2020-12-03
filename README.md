![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlippiCodeMan/CodeManBot/Java%20CI%20with%20Gradle)
![License](https://img.shields.io/github/license/SlippiCodeMan/CodeManBot)
![Discord API](https://img.shields.io/badge/discord%20api-JDA-%23843fd1)
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
- `CODEMAN_DB_URI` to a [mongodb](https://www.mongodb.com/) database access url.
- `CODEMAN_PROD_TOKEN` to a discord bot token.
- `CODEMAN_DEV_TOKEN` (optional) to a test bot token.
- `CODEMAN_EXEC_MODE` to PROD or DEV

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

To start your container:
```bash
gradle dockerStart
```

> Use `sudo` in front of those commands if you get a permission error.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)