# JavaCard Libraries

Local `*.jar` dependencies repository.

You can add here local dependencies if there are not available on the 
Maven central repository or you are not willing to use those.

If there is a `test.jar` file you can add it as a dependency
by adding the following line to the `dependencies {}` block.

```gradle
compile name: 'test'
```

This works only for JAR files placed right in the `/libs` directory.
For subdirectories you have to use the `file()` or `fileTree` as demonstrated below.

## `globalplatform-2_1_1`

Globalplatform libraries

```gradle
compile fileTree(dir: rootDir.absolutePath + '/globalplatform-2_1_1', include: '*.jar')
```

License: no idea

## `visa_openplatform`

```gradle
compile fileTree(dir: rootDir.absolutePath + '/visa_openplatform-2_0', include: '*.jar')
```

License: no idea

