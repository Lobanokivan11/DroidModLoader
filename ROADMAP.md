# Droid Mod Loader Roadmap

Droid Mod Loader is an Android mod manager focused on Bethesda games running through shared-storage Android/Windows-container workflows such as GameNative. This roadmap is ordered so major systems build on each other instead of forcing rewrites later.

The main goals are:

- reliable mod installation and deployment
- safe support for existing manual modlists
- tester-friendly diagnostics
- GameNative-focused workflows
- performance on large mod archives
- touch-friendly MO2-style organization
- future guide, collection, and validation support

---

## Phase 1 — Stabilize Current Build

**Goal:** Make the current app reliable before adding more surface area.

- Fix current compile/layout issues.
- Test profile creation, switching, and deletion.
- Test ZIP, 7z, and RAR imports.
- Test BAIN/FOMOD basic installer choices.
- Test simulated deployment.
- Test real target folder deployment.
- Test plugin scanning and export.
- Test Overwrite baseline behavior.
- Test Skyrim LE and Fallout New Vegas regression paths.
- Test Oblivion and Fallout 3 profile/plugin detection.

---

## Phase 2 — Fix Deployment Trust Issues

**Goal:** Prevent the app from saying “deployed” when files were not actually written to the selected real folder.

- Add target-scoped deployment manifests.
- Add target-scoped Data baselines.
- Add per-game simulated deployment folders.
- Force a full redeploy when target identity changes.
- Log deployment target identity in diagnostics.
- Add a manual **Force Full Redeploy** recovery action later.

### Bug Being Addressed

Simulated deploy can make the app think files already exist in the real target folder. Changing the target folder must force a real redeploy.

---

## Phase 3 — Existing Manual Modlist Adoption

**Goal:** Make Droid Mod Loader useful for users who already manually installed mods.

- Add an Existing Manual Modlist Adoption Wizard.
- Detect existing modded Data folders.
- Create a baseline from the existing Data folder.
- Add **Manage future mods only** mode.
- Detect unmanaged plugins.
- Detect unmanaged loose files, BSAs, configs, and script extender files.
- Create a read-only **Existing Manual Install** pseudo-mod.
- Import existing `plugins.txt` / `loadorder.txt` if provided.
- Later: create a managed mod from selected existing Data files.
- Later: split existing manual files into mod candidates.

### Safe Default

Do not move or delete existing manual files. Treat them as baseline. Only manage future Droid Mod Loader installs unless the user opts in.

---

## Phase 4 — Game Folder Validator

**Goal:** Catch wrong folder selection even though the UI already tells users to pick the Data folder.

- Validate selected Data folder.
- Skyrim LE should contain `Skyrim.esm`.
- Fallout New Vegas should contain `FalloutNV.esm`.
- Fallout 3 should contain `Fallout3.esm`.
- Oblivion should contain `Oblivion.esm`.
- Warn if the user picked the parent game folder instead of Data.
- Allow advanced override.

---

## Phase 5 — Settings Screen

**Goal:** Move optional and advanced controls out of the main dashboard.

- Add Settings button.
- Create Settings screen/dialog.
- Separate App Settings from Profile Settings.
- Move second-screen toggle into Settings.
- Move developer/performance options into Settings.
- Move advanced recovery actions into a Developer/Recovery section.

### Planned Settings Structure

```text
Settings
├── App Settings
│   ├── Appearance
│   ├── Experimental
│   ├── Diagnostics & Reports
│   ├── Storage
│   ├── Developer Options
│   └── About
│
└── Active Profile Settings
    ├── Game & Target
    ├── Deployment
    ├── Plugins
    ├── Overwrite
    ├── Existing Manual Install
    ├── GameNative Environment
    ├── Config Presets
    └── Collections
```

---

## Phase 6 — Appearance, Dark Mode, and Themes

**Goal:** Make the app feel polished without scattering colors/styles everywhere.

- Add dark mode: System / Light / Dark.
- Add theme system.
- Add default theme.
- Add Skyrim Nordic theme.
- Add Fallout Terminal theme.
- Add Fallout Vault-Tec theme.
- Add Oblivion parchment/gold theme.
- Add reusable border/card/button style tokens.
- Add compact rows option.
- Add reduced animations option.

---

## Phase 7 — Diagnostics and Support Exports

**Goal:** Make tester reports easier to read and debug.

- Export `DML_Modlist_Report.txt`.
- Export `DML_Support_State.json`.
- Add one-tap Support Bundle later.
- Include app version/build label.
- Include device info.
- Include active profile/game.
- Include target identity.
- Include managed mods.
- Include plugin load order.
- Include official/unmanaged plugin labels.
- Include Overwrite state.
- Include deployment manifest info.
- Include recent logs.
- Include operation timings.
- Include GameNative environment if imported.
- Add path redaction option.

### Export Files

```text
DML_Modlist_Report.txt  = human-readable support report
DML_Support_State.json  = machine-readable debugging state
```

---

## Phase 8 — Operation Timing and Logging

**Goal:** Measure slow operations before optimizing them.

- Timestamp every log line.
- Add operation duration in milliseconds.
- Add operation timeline JSON.
- Measure archive copy time.
- Measure archive extraction time.
- Measure installer analysis time.
- Measure mod indexing time.
- Measure plugin sync time.
- Measure deployment diff time.
- Measure deploy copy time.
- Measure manifest save/load time.
- Measure overwrite scan time.

### Example Log Style

```text
[18:28:01.123] Import archive started: Noble Skyrim Full Pack 2K
[18:33:42.884] Import archive finished (341761 ms)
[18:35:04.010] Deploy started
[18:41:11.222] Deploy finished (367212 ms)
```

---

## Phase 9 — Crash Recovery and Deployment Verification

**Goal:** Make failures diagnosable after app restart or incomplete deployment.

- Add crash recovery marker.
- Record active operation phase.
- Detect unfinished operation on next launch.
- Show recovery report.
- Add target folder write test.
- Add post-deploy verification pass.
- Verify deployed files exist.
- Verify deployed file sizes when possible.
- Report missing deployed files.
- Add stable error codes.

### Example Error Codes

```text
DML-IMPORT-001 Unsupported archive format
DML-DEPLOY-002 Target folder not writable
DML-SAF-005 Tree URI write failed
DML-BASELINE-006 Baseline target mismatch
```

---

## Phase 10 — Storage Manager and Integrity Audit

**Goal:** Keep Android storage under control and detect corrupted app state.

- Show app storage used.
- Show installed mods size.
- Show imported archive cache size.
- Show temp/session files size.
- Show logs/reports size.
- Clear temp files.
- Clear old installer sessions.
- Clear archive cache.
- Clean orphaned mod folders.
- Detect mod record with missing folder.
- Detect folder with no mod record.
- Detect plugin source mod missing.
- Detect manifest references to missing files.
- Detect duplicate mod/plugin priorities.
- Detect stale baseline for old target.

---

## Phase 11 — Performance Overlay and Large Mod Optimization

**Goal:** Make large mods like Noble Skyrim practical.

- Developer performance overlay.
- RAM usage display.
- Free app storage display.
- Current operation display.
- Elapsed time display.
- Files processed display.
- Bytes processed display.
- MB/sec estimate.
- Import/deploy progress phases.
- Avoid redundant archive copies.
- Stream archive extraction where possible.
- Persistent hash cache.
- Incremental deployment improvements.
- Bounded parallel copy/hash for local file deployment.
- Better SAF/Tree URI performance strategy.
- Progress UI for huge archive operations.

### Current Large Mod Benchmark

```text
~5 minutes uncompressing
~6 minutes deploying
~11 minutes total for one large mod
```

---

## Phase 12 — MO2-Style Separators and List Organization

**Goal:** Make large modlists manageable on touch screens.

- Add separators / section headers.
- Rename separators.
- Move separators.
- Collapse/expand separators.
- Assign mods to separators.
- Drag mods between separators.
- Bulk move selected mods to separator.
- Show mod count per separator.
- Show conflict/warning badges per separator.
- Add search/filter for mods.
- Add mod tags/categories.
- Add mod notes.

### Touch-Friendly MO2 Adaptation

- Long-press actions.
- Large drag handles.
- Bulk selection.
- Expandable sections.
- Badges instead of dense desktop tables.

---

## Phase 13 — Mods/Plugins Panel Polish

**Goal:** Make the large floating panels feel like real management screens.

- Improve drag reorder feel.
- Add visual drop position.
- Add auto-scroll while dragging.
- Add search/filter.
- Add bulk enable/disable.
- Add plugin source filters.
- Filter official / managed / unmanaged / overwrite plugins.
- Show missing master warnings later.
- Show conflict/update/warning badges.
- Add better locked official plugin display.

---

## Phase 14 — View Files and Conflict Visualization

**Goal:** Make conflicts understandable.

Current foundation:

- Folder summaries.
- Winning/overwritten counts.
- Color-highlighted groups.

Still planned:

- Nested expandable file tree.
- Show winning mod.
- Show overwritten-by mod.
- Show “this mod overwrites X.”
- Show “this mod is overwritten by Y.”
- Show conflict badges on mod rows.
- Separate loose-file and archive/BSA visibility.
- Show suspicious setup-only files.
- Show optional installer leftovers.

---

## Phase 15 — Overwrite Management

**Goal:** Move from Overwrite detection to real MO2-style management.

Current foundation:

- Automatic Data baseline.
- Overwrite candidates = new/changed files after baseline.
- Overwrite panel.

Still planned:

- Show Overwrite as pseudo-mod at bottom.
- Create Mod From Overwrite.
- Move selected overwrite files to existing mod.
- Delete selected overwrite files.
- Ignore selected overwrite files.
- Show overwrite file tree.
- Track generated output mods.
- Warn when Overwrite is dirty.

---

## Phase 16 — Safe Mode and Recovery Mode

**Goal:** Give testers a way to recover without resetting everything.

- Disable all mods.
- Disable all non-official plugins.
- Restore last known deployment manifest.
- Clear temp installer sessions.
- Rebuild dashboard state.
- Force plugin resync.
- Force full redeploy.
- Export emergency diagnostics.
- Clear broken pending installer state.

---

## Phase 17 — GameNative Integration Track

**Goal:** Make Droid Mod Loader fit GameNative workflows better without relying on private/sandboxed internals.

- GameNative Workspace Wizard.
- GameNative Compatibility Profile.
- GameNative Config Import / Environment Scanner.
- GameNative Run Pack / Config Helper.
- Export `ConfigExport` folder.
- Export plugin/loadorder/config files.
- Windows-side helper script/exe later.
- Detect mismatched game executable/profile.
- Include GameNative environment in reports.
- Store Proton/DXVK/Turnip/FEX/WOWBox64 notes.

Removed from roadmap:

```text
xLODGen bridge / built-in xLODGen
```

---

## Phase 18 — Config Presets

**Goal:** BethINI-style presets and GameNative-friendly INI export.

- Config preset model.
- `Skyrim.ini` / `SkyrimPrefs.ini` presets.
- `Fallout.ini` / `FalloutPrefs.ini` presets.
- Fallout 3 presets.
- `Oblivion.ini` presets.
- Performance preset.
- Balanced preset.
- Quality preset.
- GameNative safe preset.
- Snapdragon/Turnip preset.
- Preview config diff.
- Export to `ConfigExport`.
- Backup existing configs through helper later.

Also tracked here:

```text
Investigate linked Droid Mod Loader config/helper issue from prior ChatGPT thread.
```

---

## Phase 19 — Project Structure Cleanup / Refactor

**Goal:** Avoid the app turning into a giant tangled MainActivity.

### Target Structure

```text
engine/
  archive/
  baseline/
  collection/
  config/
  deploy/
  game/
  gamenative/
  index/
  install/
  overwrite/
  plugin/
  profile/
  report/
  settings/
  util/

ui/
  components/
  dialogs/
  panels/
  screens/
  settings/
  theme/
```

### Move Workflows Out of MainActivity

- `ImportWorkflow`
- `DeployWorkflow`
- `PluginWorkflow`
- `ProfileWorkflow`
- `OverwriteWorkflow`
- `DiagnosticsWorkflow`
- `SettingsWorkflow`
- `CollectionWorkflow`

### Refactor Goals

- Reduce duplicate models.
- Centralize supported game metadata.
- Centralize operation timing.
- Centralize target identity.
- Cache repeated scans.
- Remove stale lesson/test code from production path.

---

## Phase 20 — Custom Droid Mod Loader Collections

**Goal:** Native portable profile/modlist recipes.

- Export current profile as collection.
- Import DML collection.
- Apply collection checklist.
- Detect missing archives.
- Detect already installed matching mods.
- Apply mod priority.
- Apply plugin priority.
- Apply enabled/disabled states.
- Store notes/warnings.
- Store expected versions/archive names.
- Add guide-mode compatibility.

First version should be checklist-focused, not auto-download.

---

## Phase 21 — Guide Mode

**Goal:** Support a Viva New Vegas-style guide flow for Skyrim LE and other future guide setups.

- Step-by-step guide flow.
- Required mods.
- Optional mods.
- Expected plugin list.
- Expected mod priority.
- Compatibility notes.
- GameNative settings notes.
- Missing archive checklist.
- Validation checks.
- Export guide support report.

---

## Phase 22 — Texture Budget and Compatibility Scoring

**Goal:** Make Android/GameNative modlists easier to judge before running.

- Texture file count.
- Texture folder size.
- Detect large texture packs.
- Detect 2K/4K-heavy mods where possible.
- Per-mod size footprint.
- Plugin count pressure.
- Script extender dependency warnings.
- Device/GameNative environment notes.
- Profile compatibility score.

### Example

```text
Texture pressure: High
Plugin count: Medium
Script extender dependency: Yes
Expected device class: Snapdragon 8 Gen 2+
```

---

## Phase 23 — xEdit Companion Bridge

**Goal:** Windows-side xEdit reports, Android-side viewer.

- Windows-side xEdit bridge concept.
- Export current load order for xEdit.
- Run xEdit/script manually through GameNative/Windows side.
- Export xEdit report to shared folder.
- Import/read report in Droid Mod Loader.
- Show missing masters.
- Show dirty plugin warnings.
- Show dependency issues.
- Include summary in support bundle.

Important: this is not a full Android xEdit port.

---

## Phase 24 — Nexus Browsing and Downloads

**Goal:** Smoother mod acquisition.

- In-app browser/WebView.
- Recognize Nexus download links.
- Download archive into imports.
- Send archive to install pipeline.
- Track Nexus mod ID/file ID metadata.
- Associate imported archive with installed mod.
- Handle large downloads safely.
- Respect Nexus account/API limitations.

---

## Phase 25 — Nexus/Vortex Collection Import

**Goal:** Convert external collections into Droid Mod Loader’s native collection format.

- Read Nexus/Vortex collection metadata.
- Convert to DML collection model.
- Show missing archive checklist.
- Match installed mods by metadata/archive name.
- Apply plugin/mod order where safe.
- Warn for unsupported installer choices.
- Keep native DML collections as source of truth.

---

## Phase 26 — Plugin Intelligence and LOOT-Like Sorting

**Goal:** Deeper plugin correctness.

- Rule-based plugin warnings.
- Missing master detection.
- Plugin dependency graph.
- Official plugin order enforcement.
- Warn if plugin enabled but source mod disabled.
- Warn if plugin source mod missing.
- Warn if BSA/plugin pairing looks wrong.
- Import xEdit report warnings.
- Metadata-based plugin sorting.
- Later: LOOT masterlist support.

---

## Phase 27 — Advanced BAIN/FOMOD

**Goal:** Make installer support robust.

Current status: basic foundation.

Still needed:

- FOMOD pages/steps.
- Conditions.
- Flags.
- Dependency checks.
- Image previews.
- Better recommended/default logic.
- Scripted installer strategy.
- BAIN subpackage grouping.
- Remember installer choices.
- Better installer preview.

---

## Phase 28 — Experimental Second-Screen Modes

**Goal:** Make dual-screen Android devices like the AYN Thor useful without making the app harder on normal phones.

Current status: experimental plugin display exists.

Future modes:

- Plugin Monitor.
- Operation Monitor.
- Performance Monitor.
- Selected Mod Details.
- Collection Checklist.
- Tester Report Panel.
- GameNative Environment Panel.
- Touch Shortcut Pad.

Best final behavior:

```text
Idle: Plugin Monitor
During import/deploy: Operation Monitor
Selected mod: Mod Details
Collection active: Collection Checklist
```

Settings should control:

```text
Enable second-screen support
Second-screen mode: Plugin / Operation / Performance / Mod Details / Auto
```

---

# Highest-Priority Next Sprint

```text
1. Target-scoped deployment manifests
2. Target-scoped baselines
3. Per-game simulated deploy folders
4. Existing manual modlist adoption planning/model
5. Game folder validator
6. Operation timing logs
7. Support report export
8. Settings screen skeleton
```

---

# Current Status Summary

## Complete Enough for Testing

- Profiles.
- Compose dashboard.
- ZIP/7z/RAR import basics.
- Two-phase archive installer foundation.
- Basic BAIN/FOMOD choices.
- Mod enable/disable/delete/reorder.
- Plugin scanning/export.
- Official plugin scan-first detection.
- Oblivion/Fallout 3 support foundation.
- Mod content indexing.
- Large floating panels/dialogs.
- Baseline-based Overwrite foundation.
- Experimental second-screen plugin display.
- Diagnostics/share logs.

## Still Experimental

- BAIN/FOMOD support.
- Overwrite management.
- Drag reorder feel.
- Second-screen support.
- Oblivion/Fallout 3 real compatibility.
- RAR support for all archive variants.
- Performance on huge mods.

## Not Started Yet

- Settings screen.
- Dark mode/theme system.
- Separators.
- Operation timing logs.
- Performance overlay.
- Storage manager.
- Manual modlist adoption wizard.
- Support bundle export.
- Config presets.
- GameNative helper/config import.
- Custom collections.
- Nexus downloads.
- Nexus/Vortex collection import.
- xEdit bridge.
- LOOT-like sorting.
- Advanced FOMOD/BAIN.
