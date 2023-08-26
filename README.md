# 🧩 Cordex [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)

**🚀 A fast, powerful and easy-to-use [Javacord](https://javacord.org/) framework written in [Kotlin](https://kotlinlang.org/).**

> [!WARNING]
> Keep in mind that this library is in heavy beta, and may not be well-suited for production purposes just yet.

## 🔌 Requirements

To build and run this project, you'll need the following prerequisites:

- **Java**: `8 or later`
- **Kotlin**: `1.9.0`
- **Coroutines**: `1.7.3`
- **Javacord**: `3.9.0`

## ✨ Examples

Setting up Cordex:

```kt
fun main() {
  cordex("TOKEN") {
    prefix { "~" }
    
    config {
      setAllIntents()
    }
  }
}
```
Registering a text command:

```kt
cordex("TOKEN") {
  prefix { "~" }
  
  config {
    setAllIntents()
  }
  
  commands {
    +ExampleCommand()
    +TestCommand()
    //...
  }
}
```

Alternatively, you can load commands from a package:

```kt
cordex("TOKEN") {
  prefix { "~" }
  
  config {
    setAllIntents()
  }
  
  commands {
    load("me.blast.commands")
  }
}
```

> [!NOTE]
> Providing a package name is not required but strongly recommended. Not doing so may negatively impact the startup overhead of the bot.

## 📝 TODO
- [ ] Add support for slash commands.
- [ ] Implement an argument parsing system.
- [ ] Implement error handling mechanism.
- [ ] Provide support for other minor features. (cooldowns, permissions, categories, help message)