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
 *   ../src/main/java/com/instructure/instui/primitives/InstUIColors.kt
 *   ../src/main/java/com/instructure/instui/primitives/InstUISizes.kt
 *   ../src/main/java/com/instructure/instui/primitives/InstUIFontWeights.kt
 *   ../src/main/java/com/instructure/instui/primitives/InstUIFontFamilies.kt
 *   ../src/main/java/com/instructure/instui/primitives/InstUIOpacities.kt
 *
 * To update to a newer version of instructure-ui, bump INSTUI_VERSION below and re-run.
 */

const https = require('https')
const buildPrimitivesConfig = require('./sd.config.primitives')

const INSTUI_VERSION = 'v11.7.1'
const TOKENS_BASE_URL = `https://raw.githubusercontent.com/instructure/instructure-ui/${INSTUI_VERSION}/packages/ui-scripts/lib/build/tokensStudio`

function download(url) {
  return new Promise((resolve, reject) => {
    https.get(url, res => {
      if (res.statusCode === 301 || res.statusCode === 302) {
        return download(res.headers.location).then(resolve).catch(reject)
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

async function buildPrimitives() {
  const url = `${TOKENS_BASE_URL}/primitives/default.json`
  console.log(`Downloading primitive tokens from ${url}...`)
  const primitives = JSON.parse(await download(url))
  console.log('Building Jetpack Compose primitives...')
  const sd = await buildPrimitivesConfig(primitives)
  await sd.buildAllPlatforms()
}

async function main() {
  console.log(`InstUI Token Generator for Android`)
  console.log(`Using instructure-ui ${INSTUI_VERSION}`)
  console.log('')

  await buildPrimitives()

  console.log('')
  console.log('Done! Generated files are in ../src/main/java/com/instructure/instui/primitives/')
}

main().catch(err => {
  console.error('Error:', err.message)
  process.exit(1)
})
