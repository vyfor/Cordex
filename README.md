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
    implementation("com.github.reblast:Cordex:0.3")
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
    <version>0.3</version>
</dependency>
```

## ✨ Examples

### Setting up Cordex

Initialize Cordex by setting your bot token. You can configure the created DiscordApiBuilder inside the `api` block.

```kt
fun main() {
  cordex("TOKEN") {
    prefix { "~" }
    
    api {
      setAllIntents()
    }
  }
}
```

### Creating a Command

There are two ways of creating commands:
1. **Through inheritance:**
    - Define a custom command by extending the `Command` class and implementing its execute function.
    ```kt
    class ExampleCommand : Command("example") {
      override suspend fun Arguments.execute(ctx: Context) {
        // Command logic goes here
        ctx.message.reply("This is an example command!")
      }
    }
    ```
2. **Through the DSL function:**
    - Define a custom command by using the `command()` function and providing necessary fields.
    ```kt
    command("example") {
      execute { ctx ->
        ctx.message.reply("This is an example command!")
      }
    }
    ```

#### Registering a text command

Add your newly created command to the command registry using the following methods:
- A plus sign followed by an instance of your command class.
- The `load("package")` method to load classes extending the `Command` class.

```kt
cordex("TOKEN") {
  //...
  
  commands {
    +ExampleCommand()
    
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

**There exist 3 types of arguments:**
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
  val positional by positional("Description") // [--positional] <value>
}
```

Names are optional; Cordex obtains them from variable names, using the first letter as the short name.

#### Accessing argument values

Argument values can be accessed in different ways:
1. `option.value`
2. `option()`
3. `option { it -> ... }`
 
### Beyond the Basics

Arguments may be optional, hold default values, accept multiple values, and have validators:

```kt
val name by option().multiple(1..2) // List<String> (min: 1, max: 2)
val age by option().optional(18) { toInt() } // Int? (default: 18)
```

Validators also offer the capability to include custom error messages, achieved by throwing an exception with a message included.

### Predefined validators

Cordex provides a range of predefined validators you can utilize:

#### Primitive types
- `int()` `uInt()`
- `long()` `uLong()`
- `float()`
- `double()`

#### Discord entities
- `user()`
- `role()`
- `category()`
- `message()`
- `customEmoji()`
- `snowflake()`
- `textChannel()` `voiceChannel()` `threadChannel()` `stageChannel()` `forumChannel()` `channel(types...)`

#### And more
- `url()`
- `duration()`
- `date()`
- `dateTime()`
- `color()`
- `unicodeEmoji()`
- `enum()`
- `map()`

> [!NOTE]
> These methods combine multiple input values (from multi-value arguments) into a single string and try to convert the combined result to the appropriate type.
> 
> If you want each input value to be converted separately, use the same function with a plural name. *e.g.* `users()` `roles()` 

### Command Suggestions
**Cordex** offers a feature known as command suggestions, allowing the bot to proactively suggest commands that closely match any incorrectly provided by the user.

To enable this functionality, simply include the following line of code:
```kt
cordex("TOKEN") {
  enableCommandSuggestion(DistanceAccuracy)
}
```
Here, the parameter `DistanceAccuracy` represents the level of precision in matching input string with defined commands.

### Pagination
You can create a paginator that goes through a given list using the `List<T>.paginate` or` List<T>.paginateDefault` function.
The `paginateDefault` function provides the same pagination features but with default handlers attached.

The syntax is as follows:
```kt
List<T>.paginate(
  channel = ctx.channel,
  messageEvent = ctx.event,
  itemsPerPage = 1,
  onStart = { messageEvent, paginator, currentItems ->  
    MessageBuilder().setContent(currentItems.joinToString("\n"))
  },
  onPagination = { message, paginator, currentItems ->
    MessageUpdater(message).setContent(currentItems.joinToString("\n"))
  },
  onEmpty = {
    MessageBuilder().setContent("No items found")
  },
  removeAfter = 2.minutes,
  canClose = true // Adds a button to close the paginator
)
```

> Take a look at [PaginationUtils.kt](src/main/kotlin/me/blast/utils/pagination/PaginationUtils.kt) to see a complete example.

### Extras
Here are some additional features offered by **Cordex**:
- Command permissions
- Command cooldowns

*(Soon)*
- Slash command support
- `Attachment` argument type
- Command categorization
- Subcommands
- Pagination

## 📚 Dependencies
**Cordex relies on these amazing libraries:**
  - https://github.com/Javacord/Javacord

    An easy to use multithreaded library for creating Discord bots in Java.

  - https://github.com/felldo/JEmoji

    Java Emoji (JEmoji) is a lightweight fast emoji library for Java with the purpose to improve and ease working with emojis

## 📝 TODO
- [ ] Add support for slash commands and attachments.
- [ ] Provide support for other minor features. (categories, pagination, and more...)
