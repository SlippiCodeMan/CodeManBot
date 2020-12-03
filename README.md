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

## Usage

To use it make sure to set a few environement variables first:
- `CODEMAN_DB_URI` to a [mongodb](https://www.mongodb.com/) database access url.
- `CODEMAN_PROD_TOKEN` to a discord bot token.

Then you can run it with:

```bash
gradle run
```

Docker support is planned.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)