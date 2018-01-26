# JavaCard Template project with Gradle

[![Build Status](https://travis-ci.org/ph4r05/javacard-gradle-template.svg?branch=master)](https://travis-ci.org/ph4r05/javacard-gradle-template)

This is simple JavaCard project template using Gradle build system.

You can develop your JavaCard applets and build cap files with the Gradle!
Moreover the project template enables you to test the applet with [JCardSim] or on the physical cards.

Gradle project contains 2 modules:

- `applet`: contains the javacard applet. This one is buildable to CAP file.
- `tester`: contains helper classes and main Test class which uses the main applet file.

Features:
 - Gradle build (CLI / IntelliJ Idea)
 - Build CAP for applets
 - Test applet code in [JCardSim] / physical cards
 - IntelliJ Idea: Coverage
 - Travis support 

### Template

The template contains simple Hello World applet generating random bytes on any APDU message received.
There is also implemented very simple test that sends static APDU command to this applet - in JCardSim.

The Gradle project can be opened and run in the IntelliJ Idea.

Running in IntelliJ Idea gives you a nice benefit: *Coverage*!

## How to use

- Clone this template repository:

```
git clone --recursive https://github.com/ph4r05/javacard-gradle-template.git
```

- Implement your applet in the `applet` module.

- Implement tests in the `tester` module.

- Run Gradle wrapper `./gradlew` on Unix-like system or `./gradlew.bat` on Windows
to build the project for the first time (Gradle will be downloaded if not installed).

## Building cap

- Setup your Applet ID (`AID`) in the `./applet/build.gradle` variable `appletId`.

- Run the `cap` task:

```
./gradlew cap  --info --rerun-tasks
```

Generates a new cap file `./applet/out/cap/applet.cap`

Note: `--rerun-tasks` is to force re-run the task even though the cached input/output seems to be up to date.

Typical output:

```
[ant:cap] [ INFO: ] Converter [v3.0.5]
[ant:cap] [ INFO: ]     Copyright (c) 1998, 2015, Oracle and/or its affiliates. All rights reserved.
[ant:cap]     
[ant:cap]     
[ant:cap] [ INFO: ] conversion completed with 0 errors and 0 warnings.
[ant:verify] XII 10, 2017 10:45:05 ODP.  
[ant:verify] INFO: Verifier [v3.0.5]
[ant:verify] XII 10, 2017 10:45:05 ODP.  
[ant:verify] INFO:     Copyright (c) 1998, 2015, Oracle and/or its affiliates. All rights reserved.
[ant:verify]     
[ant:verify]     
[ant:verify] XII 10, 2017 10:45:05 ODP.  
[ant:verify] INFO: Verifying CAP file /Users/dusanklinec/workspace/jcard/applet/out/cap/applet.cap
[ant:verify] javacard/framework/Applet
[ant:verify] XII 10, 2017 10:45:05 ODP.  
[ant:verify] INFO: Verification completed with 0 warnings and 0 errors.
```

## Running tests

```
./gradlew :tester:test --info --rerun-tasks
```

Output:

```
Running test: Test method hello(AppletTest)

Gradle suite > Gradle test > AppletTest.hello STANDARD_OUT
    Connecting to card... Done.
    --> [00C00000080000000000000000] 13
    <-- 51373E8B6FDEC284DB569204CA13D2CAA23BD1D85DCAB02A0E3D50461E73F1BB 9000 (32)
    ResponseAPDU: 34 bytes, SW=9000
```

## Dependencies

This project uses mainly:

- https://github.com/martinpaljak/ant-javacard
- https://github.com/martinpaljak/oracle_javacard_sdks
- https://github.com/licel/jcardsim
- Petr Svenda scripts 

Big kudos for a great work!

### JavaCard support

Thanks to Martin Paljak's [ant-javacard] and [oracle_javacard_sdks] we support:

- JavaCard 2.1.2
- JavaCard 2.2.1
- JavaCard 2.2.2
- JavaCard 3.0.3
- JavaCard 3.0.4
- JavaCard 3.0.5u1

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

## Roadmap

TODOs for this project:

- Polish Gradle build scripts
- Add basic libraries as maven dependency.

## Contributions

Community feedback is highly appreciated - pull requests are welcome!



[JCardSim]: https://jcardsim.org/
[ant-javacard]: https://github.com/martinpaljak/ant-javacard
[oracle_javacard_sdks]: https://github.com/martinpaljak/oracle_javacard_sdks

