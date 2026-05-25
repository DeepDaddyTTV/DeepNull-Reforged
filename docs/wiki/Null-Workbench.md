# Null Workbench

The Null Workbench is the dedicated block for special Null crafting, Synchronizer backup and restore, and style customization.

## Tabs

- **Craft**: reduced-cost workbench recipes
- **Sync**: copy or restore a Null configuration through a Synchronizer
- **Style**: customize frame and glass colors
- **Seed**: seed DeepNull slots, DampNull tanks, and DenNull entity templates

## Notes

- Automation defaults to the crafting side only
- Style output appears in the right output slot
- Sync uses the two large slots on the left for input and the two large slots on the right for output
- Copy and restore both take 3 seconds and return the Null plus Synchronizer on the right side
- Seed works with DeepNull, DampNull, and DenNull stacks
- Seed presets appear in a searchable list with built-in, generated modpack, and user-saved presets
- Selecting a preset updates the preview only; Apply writes templates or rules
- Deep Seed entries write ghost reservations only; they never create items or overwrite real stored stacks
- Damp Seed entries write fluid or chemical tank templates without filling tanks
- Den Seed entries write entity templates without creating captured entities
- User-saved Seed presets are stored in the world and are shared with other players in that save
- Workbench DeepNull recipes use the tier item, coal blocks, glass, and matching dye
- Workbench DampNull recipes use a same-tier DeepNull and a Bucket Upgrade

## Crafting

Pattern:

```text
 F
PPP
I I
```

- `F`: any Null frame
- `P`: any wooden planks
- `I`: iron block
