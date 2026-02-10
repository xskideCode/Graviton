# Graviton

Graviton is a high-performance, advanced particle effect library and plugin for Minecraft (Paper/Spigot). It leverages modern Minecraft features like **Display Entities** (Text, Item, Block) to create complex, highly customizable visual effects that go beyond standard particle limitations.

## Features

- **Display Entity Rendering**: Uses Text, Item, and Block displays for high fidelity, scalable, and full-RGB particles.
- **Advanced Physics**: Custom physics engine supporting gravity, drag/air resistance, radial velocity, and vector fields.
- **Flexible Emitters**: Support for various shapes (Point, Sphere, Circle, Box, Line, Cone) and emission types (Continuous, Burst, Trail).
- **Dynamic Properties**: Control particle color, scale, and velocity over time using gradients and curves.
- **Resource Pack Integration**: Render custom textures and images via resource pack font mappings.

## Installation

1.  Download the `graviton-bukkit` JAR file from the releases page (or build from source).
2.  Place the JAR into your server's `plugins` folder.
3.  (Optional) Install the `graviton-resourcepack.zip` on your client or server for custom texture support.
4.  Restart your server.

## Commands

### `/graviton spawn <type> <shape> [rate] [duration]`

Spawns a configurable particle emitter at your location.

*   `type`: `CONTINUOUS`, `BURST`, `TRAIL`
*   `shape`: `POINT`, `SPHERE`, `CIRCLE`, `BOX`, `LINE`, `CONE`
*   `rate`: Particles per second (default: 20)
*   `duration`: Duration in seconds (0 = infinite)

**Example:**
`/graviton spawn CONTINUOUS SPHERE 50 10` - Spawns a sphere of particles for 10 seconds.

### `/graviton effect <name>`

Spawns a pre-configured complex effect.

*   `fire`: Realistic upward-moving fire with color gradients.
*   `fire2`: Tapered flame effect with smoke transition.
*   `explosion`: Radial burst with rapid fade-out.
*   `star`: Twinkling star burst.
*   `cat`: Renders a custom cat image (requires resource pack).
*   `void`: Dark purple floating void particles.

### `/graviton killall`

Removes all active emitters and particles from the world.

## Developer API

Graviton exposes a powerful API for developers to create custom effects.

### Key Concepts

*   **EmitterConfig**: Defines *how* particles are emitted (Rate, Shape, Burst Count).
*   **ParticleConfig**: Defines *what* the particle looks like and how it behaves (Lifetime, Color, Scale, Physics).
*   **EmitterManager**: Manages the lifecycle of all emitters.

### Example Usage (Kotlin)

```kotlin
val emitterConfig = EmitterConfig(
    type = EmitterType.BURST,
    burstCount = 20,
    shape = EmitterShape.Sphere(2.0)
)

val particleConfig = ParticleConfig(
    lifetime = ValueRange.uniform(1.0, 3.0),
    color = ColorRange.constant(Color(0.0, 1.0, 1.0, 1.0)), // Cyan
    scale = ValueGradient(Keyframe(0.0, 0.5), Keyframe(1.0, 0.0)), // Shrink
    renderMode = RenderMode.TEXT,
    texture = " " // Pixel mode
)

// Spawn at location
val uuid = emitterManager.createEmitter(location, emitterConfig, particleConfig)
```

## Building from Source

This project uses Gradle.

1.  Clone the repository.
2.  Run `./gradlew build`.
3.  The output JAR will be in `graviton-bukkit/build/libs`.

## License

[Add License Here]
