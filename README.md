# JavaCard Template project with Gradle

This is simple JavaCard project template using Gradle build system.

You can develop your JavaCard applets and build cap files with the Gradle!
Moreover the project template enables you to test the applet with [JCardSim] or on the physical cards.

Gradle project contains 2 modules:

- applet: contains the javacard applet. This one is buildable to CAP file.
- tester: contains helper classes and main Test class which uses the main applet file.

The template contains simple Hello World applet generating random bytes on any APDU message received.
There is also implemented very simple test that sends static APDU command to this applet - in JCardSim.

The Gradle project can be opened and run in the IntelliJ Idea.

Running in IntelliJ Idea gives you a nice benefit: *Coverage*!

## How to use

- Clone this template repository:

```
git clone https://github.com/ph4r05/javacard-gradle-template.git

# Initialize & fetch submodules
cd javacard-gradle-template
git submodule init
git submodule update
```

- Implement your applet in the `applet` module.

- Implement tests in the `tester` module.

## Building cap

- Setup your Applet ID (`AID`) in the `./applet/build.gradle` variable `appletId`.

```
./gradlew cap
```

Generates a new cap file `./applet/out/cap/applet.cap`

## Running tests

```
./gradlew :tester:test --info --rerun-tasks
```

## Dependencies

This project uses mainly:

- https://github.com/martinpaljak/ant-javacard
- https://github.com/martinpaljak/oracle_javacard_sdks
- https://github.com/licel/jcardsim
- Petr Svenda scripts 

Big kudos for a great work!


## Coverage

This is a nice benefit of the IntelliJ Idea - gives you coverage 
results out of the box. 

You can see the test coverage on your applet code.

- Go to Gradle plugin in IntelliJ Idea
- Navigate to `:tester` project
- Tasks -> verification -> test
- Right click - run with coverage.

Coverage summary:
![coverage summary](https://raw.githubusercontent.com/ph4r05/javacard-gradle-template/master/.github/image/coverage_summary.png)

Coverage code:
![coverage code](https://raw.githubusercontent.com/ph4r05/javacard-gradle-template/master/.github/image/coverage_class.png)

## Troubleshooting

If you experience the following error: 

```
java.lang.VerifyError: Expecting a stackmap frame at branch target 19
    Exception Details:
      Location:
        javacard/framework/APDU.<init>(Z)V @11: ifeq
      Reason:
        Expected stackmap frame at this location.
```

Then try running JVM with `-noverify` option.

In the IntelliJ Idea this can be configured in the top tool bar
with run configurations combo box -> click -> Edit Configurations -> VM Options.

[JCardSim]: https://jcardsim.org/

## Roadmap

TODOs for this project:

- Polish Gradle build scripts
- Add basic libraries as maven dependency.

## Contributions

Community feedback is highly appreciated - pull requests are welcome!


