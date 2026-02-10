# Graviton Project Summary & Development Log

## 1. Project Overview
**Graviton** is a high-performance, advanced particle effect library and plugin for Minecraft (Paper/Spigot). It leverages modern Minecraft features like **Display Entities** (Text, Item, Block) to create complex, highly customizable visual effects that go beyond standard particle limitations.

## 2. Core Architecture
-   **Emitter System**: Controls how and when particles are spawned.
    -   Types: `CONTINUOUS` (stream), `BURST` (explosion), `TRAIL` (follows entity).
    -   Shapes: `Point`, `Sphere`, `SphereFilled`, `Circle` (more extensible).
-   **Particle System**: Handles the lifecycle and physics of individual particles.
    -   Properties: Lifetime, Color (Gradient), Scale (Curve), Velocity, Gravity, Drag, Rotation.
    -   Physics: Custom physics engine supporting gravity, air resistance, and radial velocity.
-   **Rendering Engine**:
    -   **DisplayParticleRenderer**: Uses `TextDisplay`, `ItemDisplay`, and `BlockDisplay` entities for rendering. This allows for:
        -   **RGB Color**: Full 24-bit color support via TextDisplay background.
        -   **Scaling**: Dynamic resizing of particles.
        -   **Custom Textures**: Support for resource pack images via `TextDisplay` characters.
    -   **LOD System**: Level of Detail system to optimize rendering based on distance (not fully active in current `DisplayParticleRenderer` but structured).

## 3. Development Log (Current Session)

### A. Critical Bug Fixes
-   **Crash Resolution**: Fixed a server crash caused by the `/graviton spawn BURST SPHERE` command.
    -   *Cause*: Excessive default particle count (50) combined with high burst rate and rapid updates.
    -   *Fix*: Reduced default burst count to 20, optimized update loops, and implemented safeguards.
-   **VS Code / Gradle Incompatibility**:
    -   *Issue*: Error "Can't use Java 21.0.10 and Gradle 7.4.2".
    -   *Fix*: Upgraded Gradle Wrapper to **8.8** to fully support Java 21.

### B. Feature Implementation
1.  **Text Display & Resource Pack Support**:
    -   Implemented `RenderMode.TEXT` in `DisplayParticleRenderer`.
    -   **"Pixel Mode"**: Uses an empty space `" "` with a background color to create flat, colored particles (like "Minecraft Dust" but scalable and RGB).
    -   **"Texture Mode"**: Uses custom characters (e.g., `\uE001`) to display images from a server resource pack.

2.  **Physics Enhancements**:
    -   **Radial Velocity**: Added `radialVelocity` to `ParticleConfig`.
    -   *Effect*: Burst and Sphere emitters now shoot particles **outward** from the center instead of them just falling straight down.
    -   **Random Initial Velocity**: Added `UniformVector` to `ValueRange` to allow randomized starting velocities for effects like Fire.

3.  **New Commands (`/graviton effect`)**:
    -   Added a new subcommand to spawn preset complex effects:
    -   `fire`: Realistic upward-moving fire with color gradients (Yellow -> Red -> Smoke).
    -   `explosion`: Burst with high radial velocity and rapid fade-out.
    -   `cat`: A test effect using a custom resource pack character (`\uE001`) to display a cat image.
    -   `star`: Twinkling star effect using the `★` character.

4.  **Visual Polish**:
    -   **Square Pixels**: Fixed the aspect ratio of "Pixel Mode" particles. Since text spaces are thin (1:2), the renderer now automatically scales the X-axis by 2x to make them look like perfect squares.
    -   **Transparency Fix**: Fixed an issue where custom textures (like the Cat) had a white background. Now, if the texture is not a space, the background is forced to transparent `(0,0,0,0)`.

### C. Resource Pack Creation
-   Created a test resource pack in `test-server/graviton-resourcepack`.
-   **Content**: Mapped the specific unicode character `\uE001` to a provided `heart.png` texture.
-   **Purpose**: To verify the custom texture rendering capabilities of the plugin.

## 4. Key Files Modified
-   **`DisplayParticleRenderer.kt`**: core logic for rendering, scaling, and handling Text/Item/Block modes.
-   **`EffectCommand.kt`**: definitions for the new Fire, Explosion, and Cat effects.
-   **`Emitter.kt`**: physics logic updates (Radial Velocity).
-   **`ParticleConfig.kt`**: added `renderMode`, `texture`, and `radialVelocity`.
-   **`ValueRange.kt`**: added `UniformVector` for randomized 3D values.
-   **`gradle-wrapper.properties`**: updated to Gradle 8.8.

## 5. Current Status
The plugin is currently **stable** and **builds successfully**.
-   **Commands**:
    -   `/graviton spawn <type> <shape>`: Spawns a default test emitter.
    -   `/graviton effect <fire|explosion|cat|star>`: Spawns specific visual presets.
    -   `/graviton killall`: Removes all active particles.
-   **Next Steps**:
    -   Integrate full resource pack hosting or forcing.
    -   Expand the DSL for creating effects via config files.
