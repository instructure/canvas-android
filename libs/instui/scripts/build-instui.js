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
 * Downloads design tokens from the instructure-ui repository (pinned to INSTUI_VERSION)
 * and generates Jetpack Compose source files for the InstUI Android library.
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
 *     ... (65 files total, auto-discovered from instructure-ui)
 *
 * To update to a newer version of instructure-ui, bump INSTUI_VERSION below and re-run.
 */

const https = require('https')
const buildPrimitivesConfig = require('./sd.config.primitives')
const buildSemanticConfig = require('./sd.config.semantic')
const buildComponentsConfig = require('./sd.config.components')

const INSTUI_VERSION = 'v11.7.1'
const TOKENS_BASE_URL = `https://raw.githubusercontent.com/instructure/instructure-ui/${INSTUI_VERSION}/packages/ui-scripts/lib/build/tokensStudio`
const COMPONENT_PATH = 'packages/ui-scripts/lib/build/tokensStudio/rebrand/component'

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

/**
 * List all JSON files in the component token directory via GitHub API.
 */
async function listComponentFiles() {
  const apiUrl = `https://api.github.com/repos/instructure/instructure-ui/contents/${COMPONENT_PATH}?ref=${INSTUI_VERSION}`
  const headers = { 'User-Agent': 'instui-token-generator' }
  if (process.env.GITHUB_TOKEN) {
    headers['Authorization'] = `token ${process.env.GITHUB_TOKEN}`
  }
  const response = await download(apiUrl, { headers })
  const files = JSON.parse(response)
  return files
    .filter(f => f.name.endsWith('.json'))
    .map(f => f.name)
}

async function buildPrimitives() {
  const url = `${TOKENS_BASE_URL}/primitives/default.json`
  console.log(`Downloading primitive tokens from ${url}...`)
  const primitives = JSON.parse(await download(url))
  console.log('Building Jetpack Compose primitives...')
  const sd = await buildPrimitivesConfig(primitives)
  await sd.buildAllPlatforms()
}

async function buildSemantics() {
  const lightUrl = `${TOKENS_BASE_URL}/rebrand/semantic/color/rebrandLight.json`
  const darkUrl = `${TOKENS_BASE_URL}/rebrand/semantic/color/rebrandDark.json`
  const layoutUrl = `${TOKENS_BASE_URL}/rebrand/semantic/layout/default.json`

  console.log(`Downloading semantic color tokens (light)...`)
  const lightColors = JSON.parse(await download(lightUrl))

  console.log(`Downloading semantic color tokens (dark)...`)
  const darkColors = JSON.parse(await download(darkUrl))

  console.log(`Downloading semantic layout tokens...`)
  const layout = JSON.parse(await download(layoutUrl))

  console.log('Building Jetpack Compose semantic tokens...')
  const sd = await buildSemanticConfig(lightColors, darkColors, layout)
  await sd.buildAllPlatforms()
}

async function buildComponents() {
  // Download primitives, layout, and semantic light colors for reference chain resolution
  const primitivesUrl = `${TOKENS_BASE_URL}/primitives/default.json`
  const layoutUrl = `${TOKENS_BASE_URL}/rebrand/semantic/layout/default.json`
  const lightColorsUrl = `${TOKENS_BASE_URL}/rebrand/semantic/color/rebrandLight.json`

  console.log('Downloading primitives for reference resolution...')
  const primitives = JSON.parse(await download(primitivesUrl))

  console.log('Downloading layout tokens for reference resolution...')
  const layout = JSON.parse(await download(layoutUrl))

  console.log('Downloading semantic light colors for reference resolution...')
  const semanticLightColors = JSON.parse(await download(lightColorsUrl))

  console.log('Listing component token files...')
  const componentFiles = await listComponentFiles()
  console.log(`Found ${componentFiles.length} component token files`)

  // Download all component JSONs in batches
  const componentBaseUrl = `${TOKENS_BASE_URL}/rebrand/component`
  const components = {}
  const BATCH_SIZE = 10

  for (let i = 0; i < componentFiles.length; i += BATCH_SIZE) {
    const batch = componentFiles.slice(i, i + BATCH_SIZE)
    const results = await Promise.all(
      batch.map(async (filename) => {
        const url = `${componentBaseUrl}/${filename}`
        console.log(`  Downloading ${filename}...`)
        const json = JSON.parse(await download(url))
        return { filename, json }
      })
    )
    results.forEach(({ filename, json }) => {
      components[filename] = json
    })
  }

  console.log('Building Jetpack Compose component tokens...')
  const sd = await buildComponentsConfig(components, layout, primitives, semanticLightColors)
  await sd.buildAllPlatforms()
}

async function main() {
  console.log(`InstUI Token Generator for Android`)
  console.log(`Using instructure-ui ${INSTUI_VERSION}`)
  console.log('')

  await buildPrimitives()
  console.log('')

  await buildSemantics()
  console.log('')

  await buildComponents()

  console.log('')
  console.log('Done! Generated files are in:')
  console.log('  ../src/main/java/com/instructure/instui/token/primitives/')
  console.log('  ../src/main/java/com/instructure/instui/token/semantic/')
  console.log('  ../src/main/java/com/instructure/instui/token/component/')
}

main().catch(err => {
  console.error('Error:', err.message)
  process.exit(1)
})
