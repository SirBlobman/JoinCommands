# Join Commands

Join Commands is a plugin for Spigot and BungeeCord that allows you to execute commands for the following events:

- A player joins the BungeeCord proxy.
- A player joins the Spigot server.
- A player joins a world within a Spigot server.

## Requirements

- Java 21

## Supported Software

| Name                                                                                                     | API Versions       | Type   |
|----------------------------------------------------------------------------------------------------------|--------------------|--------|
| [Spigot](https://www.spigotmc.org/) or [Paper](https://papermc.io/downloads/paper)                       | 1.21-R0.1-SNAPSHOT | Server |
| [BungeeCord](https://ci.md-5.net/job/BungeeCord/) or [Waterfall](https://papermc.io/downloads/waterfall) | 1.21-R0.1-SNAPSHOT | Proxy  |
| [Velocity](https://papermc.io/downloads/velocity)                                                        | 3.3.0-SNAPSHOT     | Proxy  |

## Installation Instructions

### Server

1. Download the `JoinCommands.jar` file.
2. Upload the file to the `/plugins/` folder of your server.
3. Restart your server. Plugin managers and `/reload` are not supported.
4. Modify the `/plugins/JoinCommands/config.yml` file to match your needs.
5. Execute the `/join-commands reload` command.

### Proxy

1. Download the `JoinCommands.jar` file.
2. Upload the file to the `/plugins/` folder of your proxy.
3. Follow the installation guide for each server linked to your proxy.
4. Enable the `bungeecord-hook` setting on each spigot server, even if your proxy is Velocity.
5. Restart the proxy to generate the configuration file.
6. Modify the `/plugins/JoinCommands/config.yml` file to match your needs.
7. Restart the proxy again.
8. Restart all servers linked to the proxy so that the messaging channels can be setup properly.

## Developers

### Compilation

You can compile this project yourself with the gradle wrapper.

#### Windows

```shell
> gradlew.bat clean build
```

#### Mac and Linux

```shell
$ ./gradlew clean build
```
