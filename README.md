# 🧩 Cordex [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org) [![](https://jitpack.io/v/ReBlast/Cordex.svg)](https://jitpack.io/#ReBlast/Cordex)

**🚀 A fast, powerful and easy-to-use [Javacord](https://javacord.org/) framework written in [Kotlin](https://kotlinlang.org/).**

**Cordex** is an innovative command framework designed to optimize the capabilities of Javacord, combining rich features and an intuitive syntax that simplifies the process of building Discord bots.
It presents a developer-friendly solution for defining command arguments in a manner reminiscent of command-line interfaces (CLI).

> [!WARNING]
> Keep in mind that this library is in beta, and may not be well-suited for production purposes just yet.

## 🔌 Requirements

In order to use Cordex, you'll need the following prerequisites:

- **Java**: `8 or later`
- **Kotlin**: `1.9.0`
- **Coroutines**: `1.7.3`
- **Javacord**: `3.9.0`

## 📦 Installation

### Gradle

```gradle
repositories {
    ...
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.reblast:Cordex:0.2.1")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.reblast</groupId>
    <artifactId>Cordex</artifactId>
    <version>0.2.1</version>
</dependency>
```

## ✨ Examples

### Setting up Cordex

Initialize Cordex by setting your bot token. You can configure the created DiscordApiBuilder inside the `config` block.

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

### Creating a Command

Define a custom command by extending the `Command` class and implementing its execute function.

```kt
class ExampleCommand : Command("example") {
  override suspend fun execute(ctx: Context, args: Arguments) {
    // Command logic goes here
    ctx.message.reply("This is an example command!")
  }
}
```

#### Registering a text command

Add your newly created command to the command registry using the plus sign followed by an instance of your command class or use the `load` method.

```kt
cordex("TOKEN") {
  //...
  
  commands {
    +ExampleCommand()
    
    // Alternatively, you can load commands from a package
    load("me.blast.commands")
  }
}
```

> [!WARNING]
> Specifying a package name isn't mandatory, but highly recommended. Not doing so may negatively impact the startup overhead of the bot.

### Error Handling

Cordex offers robust error handling capabilities to ensure your bot can gracefully manage unexpected situations.

#### Command execution error

Use `onError` to define how your bot responds when an exception occurs during command execution. 

```kt
cordex("TOKEN") {
  onError { event, command, ex ->
    // Error handling logic goes here
    event.message.reply("Error!")
  }
}
```

#### Argument parsing error

Cordex defaults to sending a usage message on argument parsing errors. However, you can customize this behavior with your own function using `onParseError`:

```kt
cordex("TOKEN") {
  onParseError { event, command, ex ->
    // Error handling logic goes here
    event.message.reply(
      when (ex) {
        is ArgumentException.Invalid -> "Invalid value provided for argument ${ex.argument.argumentName}"
        is ArgumentException.Empty -> "No value provided for argument ${ex.argument.argumentName}"
        is ArgumentException.Insufficient -> "Insufficient amount of values provided for argument ${ex.argument.argumentName}"
        is ArgumentException.Missing -> "Missing required arguments: ${ex.arguments.joinToString(", ") { it.argumentName }}"
      }
    )
  }
}
```

### Command Arguments

> Cordex follows the approach of handling arguments in a way similar to how command-line interfaces (CLI) do.

#### There exist 3 types of arguments:
- **Options**
  - Options are arguments capable of accepting values.
- **Flags**
  - Flags are arguments that are optional, and providing them assigns their value as true.
- **Positional arguments**
  - Positional arguments are arguments provided without explicit naming.

You can define each of these argument types using their respective functions:

```kt
class ExampleCommand : Command("example") {
  val option by option("Description") // --option, -o <value>
  val flag by flag("Description") // --flag, -f
  val positional by positional("Description") // <value>
  
  override suspend fun execute(ctx: Context, args: Arguments) {
    // And access them using args[ARGUMENT]
    ctx.message.reply(args[option])
  }
}
```

Names are optional; Cordex obtains them from variable names, using the first letter as the short name.
 
### Beyond the Basics

Arguments may be optional, hold default values, accept multiple values, and have validators:

```kt
val name by option().multiple(1..2) // List<String> (min: 1, max: 2)
val age by option().optional(18) { toInt() } // Int? (default: 18)
```

#### Predefined validators

Cordex provides a range of predefined validators you can utilize:

**Primitive types:**
`int()`
`long()`
`float()`
`double()`

**Reference types:**
`url()`
`duration()`
`color()`

**And more:**
`user()`
`channel()`
`category()`
`role()`
`unicodeEmoji()`
`customEmoji()`

> [!NOTE]
> These methods combine multiple input values (from multi-value arguments) into a single string and try to convert it to the appropriate type.
> 
> If you want each input value to be converted separately, use the same function with a plural name. *e.g.* `users()` `roles()` 

## 📚 Dependencies
**Cordex relies on these amazing libraries:**
  - https://github.com/Javacord/Javacord

    An easy to use multithreaded library for creating Discord bots in Java.

  - https://github.com/felldo/JEmoji

    Java Emoji (JEmoji) is a lightweight fast emoji library for Java with the purpose to improve and ease working with emojis

## 📝 TODO
- [ ] Add support for slash commands and attachments.
- [ ] Provide support for other minor features. (cooldowns, permissions, categories, help message)
