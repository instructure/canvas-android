//
// This file is part of Canvas.
// Copyright (C) 2021-present  Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
//

const { spawnSync } = require('child_process')

const JiraClient = require('jira-client')
const client = new JiraClient({
    protocol: 'https',
    host: 'instructure.atlassian.net',
    username: process.env.JIRA_USERNAME,
    password: process.env.JIRA_API_TOKEN,
  })

const capitalize = (s) => {
    if (typeof s !== 'string') return ''
    return s.charAt(0).toUpperCase() + s.slice(1)
  }

let delimiter = '#---------------praise the sun---------------#'

function run (cmd, args) {
    const { error, stderr, stdout } = spawnSync(cmd, args, { encoding: 'utf8' })
    if (stderr) { console.error(stderr) }
    if (error) { throw error }
    return stdout
}

addFixVersion().catch((err) => {
    console.error('Add fix versions failed:', err)
    process.exit(2)
})

async function addFixVersion() {
    console.log("-------- Starting adding fix versions to Jira tickets -------")
    const tag = process.argv[2]
    const splitTag = tag.split("-")
    const appName = splitTag[0]
    const capitalizedAppName = capitalize(appName)
    let version = splitTag[1]
    version = `Android ${capitalizedAppName} ${version}`
    const issues = await getIssues(appName, tag)
    console.log(version)
    console.log(issues)
    let fixVersionFound = false
    for (const fixVersion of await client.getVersions('MBL')) {
        if (version == fixVersion.name) {
            console.log("Fix version found")
            fixVersionFound = true
            break
        }
    }
    if (fixVersionFound) {
        await Promise.all(issues.map(issue => client.updateIssue(issue, {
            update: { fixVersions: [ { add: { name: version } } ] }
        })))
    } else {
        throw new Error(`${version} not found`)
    }
}

async function getIssues(app, tag) {
    run('git', [ 'fetch', '--force', '--tags' ])

    console.log(app)

    const tags = run('git', [ 'ls-remote', '--tags', '--sort=v:refname', 'origin', `refs/tags/${app}-*` ])
        .split('\n').map(line => line.split('/').pop()).filter(Boolean)
    const currentIndex = tags.indexOf(tag)
    const sinceTag = tags[currentIndex - 1]
    let result = run('git', [ 'log', `${sinceTag}...${tag}`, `--pretty=format:commit:%H%n%B${delimiter}`, '--' ])
    return parseGitLog(result, app.toLowerCase(), tag)
}

async function parseGitLog (log, app, tag) {
    let commits = log.split(delimiter).map(item => item.trim()).filter(a => a)
    let allJiras = []
    for (let commit of commits) {
      let apps = getAppsAffected(commit)
      if (apps.includes(app)) {
        let jiras = getJiras(commit)
        if (jiras.length) {
          allJiras.push(...jiras)
        }
      }
    }

    return allJiras
  }

function getJiras (commit) {
    const refs = /refs:(.+)/gi.exec(commit)
    if (!refs) {
      return []
    }
  
    let jiras = refs[1].split(',').map(a => a.trim()).filter(a => a !== 'none')
    return jiras
}

function getAppsAffected (commit) {
    const affects = /affects:(.+)/gi.exec(commit)
    if (!affects) {
      return []
    }
  
    let apps = affects[1]
    if (!apps) {
      return []
    }
  
    return apps.split(',').map(item => item.trim().toLowerCase()).filter(item => item !== 'none')
}
