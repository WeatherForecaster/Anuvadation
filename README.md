# Anuvadation

A multilingual Discord translation bot built with Java and JDA.

Anuvadation automatically detects the language of a message and translates it into the other configured language channels, making communication easier in multilingual Discord servers.

## Features

- Automatic language detection
- Automatic message translation
- Separate channels for different languages
- Supports English, Hindi, and Marathi
- Admin-only `/setup` command for channel configuration
- Persistent server configuration using SQLite
- Translation using the MyMemory Translation API
- Discord message handling using JDA
- Environment-variable based Discord bot token
- Maven-based project
- Standalone executable JAR for deployment

## How It Works

Anuvadation connects language-specific Discord channels together.

For example:

```text
#english
   │
   ├──→ #hindi
   └──→ #marathi