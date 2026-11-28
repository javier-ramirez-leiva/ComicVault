# Theming

ComicVault uses CSS variables to provide a consistent and easily
customizable appearance.

Color palettes and light/dark modes are independent. Each palette
is automatically compatible with both appearance modes.

## Adding a new theme

### 1. Define the color palette

In `src/styles.css`, add a new palette alongside the existing ones.

For example, to create a Ruby theme:

```css
html[data-palette='ruby'],
[data-preview-palette='ruby'] {
  --accent-hue: 25;
  --accent-chroma: 0.15;
}
```

Colors are based on the OKLCH color space:

- `--accent-hue`: Determines the color (0–360).
- `--accent-chroma`: Determines the color intensity.

Suggested hue values:

| Color  | Hue |
| ------ | --- |
| Red    | 25  |
| Orange | 50  |
| Yellow | 100 |
| Green  | 145 |
| Teal   | 180 |
| Blue   | 245 |
| Violet | 295 |
| Pink   | 340 |

A chroma between `0.10` and `0.18` is a good starting point.

### 2. Register the theme

Add the new palette to the `COLOR_PALETTES` collection in
`darkmode.service.ts`, following the structure of the existing entries.

The palette identifier must match the one defined in CSS.

Once registered, the theme will automatically appear in the
appearance settings.

### 3. Update the initialization

Add the new palette identifier to the supported palettes list
in `src/index.html`.

This ensures that the selected theme is correctly restored when
the application starts.

### 4. Test the theme

- Verify its appearance in both light and dark mode.
- Check text readability, buttons, menus and interactive states.
- Reload the application to verify preference persistence.

## Customizing an existing theme

Modify its CSS variables in `src/styles.css`.

Additional variables, such as backgrounds, surfaces, borders
and text colors, can also be overridden to create a more
distinctive appearance.

## Guidelines

- Keep color definitions centralized in `src/styles.css`.
- Use existing semantic CSS variables and Tailwind utilities.
- Avoid hardcoded colors in components.
- Maintain sufficient contrast in both appearance modes.
- Preserve the meaning of success, warning and error colors.
- Keep palette identifiers consistent across all three files.

No additional dependencies or backend changes are required.
