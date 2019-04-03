# SoSeedy CLI
A command line interface user for seeding large data sets into [Canvas LMS].
This is intended to assist in manual testing of Instructure mobile apps
and replace the [legacy Ruby CLI] for data seeding.

Seeding tasks in this CLI are implemented using [Kotlin Coroutines] in order
to seed large amounts of data quickly.

## Build
A `.jar` file can be built using gradle or a helper script included in the repo.
The `.jar` file will be located in `build/libs` when running `gradle`.
The `.jar` file will be located in `tools/` when running the helper script.

`gradle task fatJar`

`./tools/update_soseedy.sh`

## Usage
The CLI uses [PicoCLI] which will display help screens when the program
is executed without proper input arguments. Valid arguments may vary
between tasks.

### Examples
```
java -jar ./tools/soseedycli.jar SeedCourses -n 10 -r teacher
```

### Results
Seeded data information will be written to a `.csv` file in a
`results` folder for future reference. File will contain information
such as `user.loginId` and `user.password` to allow testers to log into
Canvas LMS with seeded users.


[Canvas LMS]: https://github.com/instructure/canvas-lms
[Kotlin Coroutines]: https://kotlinlang.org/docs/reference/coroutines.html
[PicoCLI]: https://github.com/remkop/picocli
[legacy Ruby CLI]: https://github.com/instructure/mobile_qa/tree/master/SoSeedy#manual-testing
