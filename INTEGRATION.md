# Using this template with existing code

This template is suitable for starting a new JavaCard project as it provides all 
required tools to start developing JavaCard applets straight away.
Readme shows how to use this template to run the HelloWorld.

Here follows the quick overview how to use this template with already existing 
JavaCard code.

## Variant A - Copy the code

This variant is suitable for no longer maintained JavaCard codes or not 
git-versioned codes. This is rather simple as just pure copying of the sources will
do the job. 

- Clone this template
- Copy the java files to `applet/src/main/java`.

## Variant B - Maintained code

With maintained code it is a bit difficult as we usually want to 
pull-request our changes to the original repository. In that case we cannot use
the copy variant A as the git history will be lost and the pull request cannot be created. 
  
Depending on the project size we have another two options:

### B.1 Use template project structure

In this case we change the existing project structure and we 
basically transform it to the structure of the template. 

It would go like this:

- Fork the existing JavaCard repository to your own account so you can do pull requests.
- Copy the template files (libs, libs-sdk, gradle, applet, ...) to the forked repository
- Move the existing code to the right place, `applet/src/main/java`
- Modify `applet/build.gradle`, define CAP file parameters as previously defined, probably in `pom.xml` (Maven) or `build.xml` (Ant)


This approach is obviously suitable for simple / small projects lacking proper gradle support. 
If the project is big and actively maintained owners probably wont be very cheerful to change the whole project structure
and another approach has to be chosen.


### B.2 Git submodule approach

If you feel that changing the project structure won't be merged via pull request it has to be done a bit differently.
This approach preserves existing project structure by embedding the project into the template project
which acts as a wrapper. The existing project is added to the wrapper as a [git-submodule].

I will demonstrate this on the example:
https://github.com/sgamerith/javacard-calculator

#### 1. Fork the project.
Fork the javacard-calculator repository to your account. 
This step is required so you can work with the existing maintained code, 
submit pull requests, etc.

My fork I use in this description is here (you will obviously use your fork address):
https://github.com/ph4r05-edu/javacard-calculator

#### 2. Clone the template 
Clone the template repository to a new directory.

```bash
git clone --recursive git@github.com:crocs-muni/javacard-gradle-template-edu.git javacard-calculator-wrapper
```

We now reinitialize the git history of the template. The reason is
you typically want to have a clean commit history without unrelated template commits.

```bash
cd javacard-calculator-wrapper
rm -rf .git
rm -rf libs-sdks/
git init
git add .
git commit -m 'Initial commit'
git submodule add https://github.com/martinpaljak/oracle_javacard_sdks.git libs-sdks
git add libs-sdks
git commit -m 'SDKs added'
```

#### 3. Test the template. 
The command will download all required dependencies. We make
sure the template works before doing major changes. 

```bash
./gradlew test
```

#### 4. Cleanup
Remove HelloWorld related files.

```bash
/bin/rm -rf applet/src
```

Our current project has working tests with JCardSim so we dont need to use 
our test/cardtools utilities. If your particular project has no test 
you may need to add tests from the scratch. The HelloWorld test suite is a good 
inspiration. You may backup `applet/src/test` somewhere if you need it later.


#### 5. Add the project as a git submodule
Add the project we are going to work on as a submodule:

```bash
# Add the project as a submodule
# In your case use your fork address.
git submodule add https://github.com/ph4r05-edu/javacard-calculator applet/project
```

#### 6. Set the source paths.
We have to adjust gradle source paths (folders with java files / packages) a bit as 
we placed the project as a submodule which has source files on a different place
than gradle expects by default. 

The main reason is we wrapped the whole project
in the `project` folder. However this approach is flexible as we don't need 
the project to follow some standard folder hierarchy.

Update `applet/build.gradle`: Add the following piece of 
code below `dependencies` section (note: not inside `buildscript` section).

```groovy
sourceSets {
    main {
        java {
            srcDir 'project/src/main/java'
        }
    }

    test {
        java {
            srcDir 'project/src/test/java'
        }
        resources {
            srcDir 'project/src/test/resources'
        }
    }
}
```

This configures gradle source directories. 
If you are using IntelliJ Idea press Gradle refresh button so 
Idea gets a new file layout also.

Gradle refresh button: right hand side vertical bar -> Gradle -> Blue refresh button.


#### 7. Fix the project

Now you may end up with a project that won't build. 
In this particular case a dependency is missing. CalculatorTest.java is missing 
`import org.apache.commons.lang3.ArrayUtils;`

By Googling you may find the following page
https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.0

Which suggests to add the following line to the `applet/build.gradle` to the `dependencies` section

```groovy
dependencies{
    compile group: 'org.apache.commons', name: 'commons-lang3', version: '3.0'
}
```

Refresh the Gradle project

Now the project is still broken. The culprit is the following line:

```java
ResponseAPDU actual = simulator.transmitCommand(commandAPDU);
```

Obviously the simulator contract for the method `transmitCommand` has changed.
We change it to the following form:

```java
ResponseAPDU actual = new ResponseAPDU(simulator.transmitCommand(commandAPDU.getBytes()));
```

Note: 
The changes performed in the test file are visible only in the submodule repository, not in the main wrapper.
Wrapper repository does not see the change so you cannot commit it to the wrapper, but you have to do it in the submodule.

#### 8. Test

Now you are ready to test the project.

#### 9. Modify Cap settings

Now you need to adapt CAP build configuration from the previous project. 
Modify `applet/build.gradle`, the `javacard` section.
Mainly adjust package and the applet class name.

Configuration directives are very similar. When you are done, build cap file

```bash
./gradlew build
```

#### 10. Push wrapper repository

Create a new GitHub repository for your wrapper. Then add the remote repository 
and push.

```bash
git remote add origin https://github.com/ph4r05-edu/javacard-calculator-wrapper.git
git push origin master
```

#### Demo

- https://github.com/ph4r05-edu/javacard-calculator-wrapper
- https://github.com/ph4r05-edu/javacard-calculator 

[git-submodule]: https://git-scm.com/docs/git-submodule
