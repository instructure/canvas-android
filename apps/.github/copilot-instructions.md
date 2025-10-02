# GitHub Copilot Preferences

## Project Context

- Canvas is a learning management system with multiple Android apps (Student, Teacher, Parent)
- The canvas-android project consists of multiple packages managed with Gradle
- The apps share a common :pandautils module for shared code
- There is a standalone module for each app (student, teacher, parent)
- The apps are written in Kotlin and use a mix of views with xml and Jetpack Compose for UI
- The apps use Retrofit and OkHttp for networking
- The apps use Room for local database storage
- The apps use Dagger Hilt for dependency injection
- The apps use Coroutines and Flow for asynchronous programming
- The apps use JUnit and Espresso for testing
- The apps follow MVVM architecture pattern
- The apps use Material Design components for UI
- The apps support multiple screen sizes and orientations
- The apps support dark mode
- The apps support multiple languages and localization

## Response Preferences

- Be concise and prioritize code examples
- Suggest Kotlin solutions with proper typing
- When suggesting code, ensure it follows existing project patterns and conventions
- When explaining code, focus on implementation details and potential edge cases
- When suggesting libraries or tools, ensure they are compatible with existing project dependencies and architecture
- When suggesting architectural changes, consider the impact on existing code and dependencies
- When suggesting UI changes, consider accessibility and responsiveness
- When suggesting testing strategies, consider existing test coverage and frameworks used
- Use Kotlin, Jetpack Compose and Kotlin Coroutines best practices appropriately
- Reference existing project patterns when suggesting new implementations
- Always use test you can see the output of.

## Tool Usage Preferences

- Prefer searching through codebase before suggesting solutions
- Use error checking after code edits
- When given a ticket identifier, use Atlassian MCP server to gather information about the task
- When given a Figma link, use Figma Desktop MCP server to gather design details

## Code Style Preferences
- Follow existing project code style and conventions
- Use consistent indentation and spacing
- Use descriptive variable and function names
- Prefer immutability where possible
- Use Kotlin idioms and best practices
- Avoid unnecessary complexity
- Ensure code is modular and reusable
- Follow SOLID principles
- Ensure proper error handling and logging
- Write unit tests for new functionality
- Write integration tests for new functionality
- Write UI tests for new functionality
- Ensure tests are isolated and repeatable
- Use mocking frameworks for dependencies in tests
- Ensure code coverage is maintained or improved with new changes
- When suggesting code snippets, ensure they are complete and can be directly used or easily integrated
- DO NOT add comments or documentation unless its specifically requested
- Code should be self-explanatory without inline comments
- When generating test files, only include license headers and the actual test code without explanatory comments

## Implementation Preferences
- Follow project's component structure and naming conventions
- Use existing utility functions and shared components when possible
- Ensure the code compiles and runs without errors
- When writing tests, make sure the tests pass
- When you are asked to write tests, ensure they are written in the same manner as existing tests in the project