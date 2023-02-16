# Join Commands
[Spigot Page](https://spigotmc.org/resources/51758/)  
[Discord Support Link](https://discord.gg/XMq2agT)

## Plugin Information
Join Commands is a plugin for Spigot and BungeeCord that allows you to execute commands for the following events:  
- A player joins the BungeeCord proxy.
- A player joins the Spigot server.
- A player joins a world within a Spigot server.

## Requirements (Spigot Server)
- Spigot 1.8.8 - 1.19.3

## Requirements (BungeeCord Proxy)
- BungeeCord or Waterfall 1.19+

## Requirements (Velocity Proxy)
- Velocity 3.1.1+

## How to install?
**Spigot Server**
1. Download the JAR from the Spigot Page linked above.
2. Upload the JAR file to your server's `/plugins/` folder.
3. Restart your server. Reloading and Plugman are not supported.
4. A `config.yml` file should be generated in your server's `/plugins/` folder. Edit this file and add/remove the commands you want.
5. Restart your server one more time. You can also do `/join-commands reload` but a restart is recommended.

**Proxy Server**
1. Download the JAR from the Spigot Page linked above.
2. Install the plugin on all your spigot servers that are linked to the proxy.
3. Upload the JAR file to your proxy's `plugins` folder.
4. Restart the proxy all servers linked to it so that the channels can be setup properly.
