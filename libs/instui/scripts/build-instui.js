#!/usr/bin/env node

/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * InstUI Token Generator for Android
 *
 * Usage: npm run build
 *
 * Reads design tokens from the @instructure/instructure-design-tokens npm package
 * (pinned in package.json) and generates Jetpack Compose source files for the
 * InstUI Android library. Icons come from the `lucide` npm package (pinned in
 * package.json) plus instructure-ui Custom icons fetched from GitHub (pinned to
 * CUSTOM_ICONS_VERSION).
 *
 * Generated files (DO NOT EDIT manually):
 *   Primitives:
 *     ../src/main/java/com/instructure/instui/primitives/InstUIColors.kt
 *     ../src/main/java/com/instructure/instui/primitives/InstUISizes.kt
 *     ../src/main/java/com/instructure/instui/primitives/InstUIFontWeights.kt
 *     ../src/main/java/com/instructure/instui/primitives/InstUIFontFamilies.kt
 *     ../src/main/java/com/instructure/instui/primitives/InstUIOpacities.kt
 *   Semantic:
 *     ../src/main/java/com/instructure/instui/semantic/InstUISemanticColors.kt
 *     ../src/main/java/com/instructure/instui/semantic/InstUIElevation.kt
 *     ../src/main/java/com/instructure/instui/semantic/InstUILayoutSizes.kt
 *     ../src/main/java/com/instructure/instui/semantic/InstUILayoutTypography.kt
 *     ../src/main/java/com/instructure/instui/semantic/InstUILayoutConfig.kt
 *   Components (one file per component, e.g.):
 *     ../src/main/java/com/instructure/instui/component/InstUIText.kt
 *     ../src/main/java/com/instructure/instui/component/InstUIHeading.kt
 *     ../src/main/java/com/instructure/instui/component/InstUIBaseButton.kt
 *     ../src/main/java/com/instructure/instui/component/InstUIPill.kt
 *     ... (auto-discovered from the design-tokens package)
 *
 * To update tokens, bump the commit SHA on @instructure/instructure-design-tokens
 * in package.json and re-run. To update icons, bump INSTUI_ICONS_VERSION below.
 */

const fs = require('fs')
const path = require('path')
const https = require('https')
const { execSync } = require('child_process')
const buildPrimitivesConfig = require('./sd.config.primitives')
const buildSemanticConfig = require('./sd.config.semantic')
const buildComponentsConfig = require('./sd.config.components')
const { buildIcons } = require('./build-instui-icons')

const CUSTOM_ICONS_VERSION = 'v11.7.1'

const TOKENS_PACKAGE_DIR = path.join(
  __dirname,
  'node_modules',
  '@instructure',
  'instructure-design-tokens',
  'tokensStudio',
)

function readTokenJson(...relativePath) {
  const fullPath = path.join(TOKENS_PACKAGE_DIR, ...relativePath)
  return JSON.parse(fs.readFileSync(fullPath, 'utf8'))
}

// The design-tokens package wraps semantic layer JSONs (rebrand/semantic/**) in a
// top-level "semantic" namespace. Style Dictionary configs (carried over from the
// previous instructure-ui setup) expect the bare structure, so unwrap when present.
function readSemanticJson(...relativePath) {
  const json = readTokenJson(...relativePath)
  return json.semantic ?? json
}

// Component JSONs in the design-tokens package reference semantic tokens with a
// "{semantic.*}" prefix (e.g. "{semantic.color.background.base}"). The SD reference
// resolver expects bare references ("{color.background.base}") to match the unwrapped
// semantic tree, so strip the prefix recursively on load.
function stripSemanticPrefix(node) {
  if (Array.isArray(node)) {
    return node.map(stripSemanticPrefix)
  }
  if (node && typeof node === 'object') {
    const result = {}
    for (const [key, value] of Object.entries(node)) {
      result[key] = stripSemanticPrefix(value)
    }
    return result
  }
  if (typeof node === 'string') {
    return node.replace(/\{semantic\.([^}]+)\}/g, '{$1}')
  }
  return node
}

// Known typos in upstream component JSON keys that need merging into their
// correctly-spelled siblings. Without this, both keys ship as disjoint objects
// in the generated tokens (e.g. InstUISideNavBarNew.InsitutionLogo and
// .InstitutionLogo each carry half the fields).
const KEY_RENAMES = {
  insitutionLogo: 'institutionLogo',
}

function applyKeyRenames(node) {
  if (!node || typeof node !== 'object' || Array.isArray(node)) return node
  const result = {}
  for (const [key, value] of Object.entries(node)) {
    const renamed = Object.prototype.hasOwnProperty.call(KEY_RENAMES, key) ? KEY_RENAMES[key] : key
    const mapped = applyKeyRenames(value)
    if (renamed in result && mapped && typeof mapped === 'object' && !Array.isArray(mapped)) {
      result[renamed] = { ...result[renamed], ...mapped }
    } else {
      result[renamed] = mapped
    }
  }
  return result
}

function readComponentJson(...relativePath) {
  return applyKeyRenames(stripSemanticPrefix(readTokenJson(...relativePath)))
}

function installDependencies() {
  // Always run npm install — it's a fast no-op when nothing has changed, and
  // correctly picks up commit-SHA bumps on git-URL dependencies (mtime checks
  // are unreliable across branch switches and SHA-only edits).
  console.log('Installing npm dependencies...')
  execSync('npm install', { cwd: __dirname, stdio: 'inherit' })
}

function download(url, options = {}) {
  return new Promise((resolve, reject) => {
    https.get(url, options, res => {
      if (res.statusCode === 301 || res.statusCode === 302) {
        return download(res.headers.location, options).then(resolve).catch(reject)
      }
      if (res.statusCode !== 200) {
        reject(new Error(`HTTP ${res.statusCode}: ${url}`))
        return
      }
      let data = ''
      res.on('data', chunk => { data += chunk })
      res.on('end', () => resolve(data))
      res.on('error', reject)
    }).on('error', reject)
  })
}

function listComponentFiles() {
  const componentDir = path.join(TOKENS_PACKAGE_DIR, 'rebrand', 'component')
  return fs.readdirSync(componentDir).filter(f => f.endsWith('.json'))
}

async function buildPrimitives() {
  console.log('Loading primitive tokens...')
  const primitives = readTokenJson('primitives', 'default.json')
  console.log('Building Jetpack Compose primitives...')
  const sd = await buildPrimitivesConfig(primitives)
  await sd.buildAllPlatforms()
}

async function buildSemantics() {
  console.log('Loading semantic color tokens (light)...')
  const lightColors = readSemanticJson('rebrand', 'semantic', 'color', 'rebrandLight.json')

  console.log('Loading semantic color tokens (dark)...')
  const darkColors = readSemanticJson('rebrand', 'semantic', 'color', 'rebrandDark.json')

  console.log('Loading semantic layout tokens...')
  const layout = readSemanticJson('rebrand', 'semantic', 'layout', 'default.json')

  console.log('Building Jetpack Compose semantic tokens...')
  const sd = await buildSemanticConfig(lightColors, darkColors, layout)
  await sd.buildAllPlatforms()
}

async function buildComponents() {
  console.log('Loading primitives for reference resolution...')
  const primitives = readTokenJson('primitives', 'default.json')

  console.log('Loading layout tokens for reference resolution...')
  const layout = readSemanticJson('rebrand', 'semantic', 'layout', 'default.json')

  console.log('Loading semantic light colors for reference resolution...')
  const semanticLightColors = readSemanticJson('rebrand', 'semantic', 'color', 'rebrandLight.json')

  const componentFiles = listComponentFiles()
  console.log(`Found ${componentFiles.length} component token files`)

  const components = {}
  for (const filename of componentFiles) {
    components[filename] = readComponentJson('rebrand', 'component', filename)
  }

  console.log('Building Jetpack Compose component tokens...')
  const sd = await buildComponentsConfig(components, layout, primitives, semanticLightColors)
  await sd.buildAllPlatforms()
}

async function main() {
  console.log('InstUI Token Generator for Android')
  console.log('')

  installDependencies()
  console.log('')

  await buildPrimitives()
  console.log('')

  await buildSemantics()
  console.log('')

  await buildComponents()
  console.log('')

  console.log('Building InstUI icons (Lucide + instructure-ui Custom)...')
  await buildIcons(download, CUSTOM_ICONS_VERSION)

  console.log('')
  console.log('Done! Generated files are in:')
  console.log('  ../src/main/java/com/instructure/instui/token/primitives/')
  console.log('  ../src/main/java/com/instructure/instui/token/semantic/')
  console.log('  ../src/main/java/com/instructure/instui/token/component/')
  console.log('  ../src/main/java/com/instructure/instui/token/icon/')
}

main().catch(err => {
  console.error('Error:', err.message)
  process.exit(1)
})
