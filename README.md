SCurator
========
An asynchronous Scala wrapper around the Apache Curator Framework.

[![Travis](https://img.shields.io/travis/granthenke/scurator.svg)](https://travis-ci.org/granthenke/scurator)
[![Codecov](https://img.shields.io/codecov/c/github/granthenke/scurator.svg)](http://codecov.io/github/granthenke/scurator?branch=master)
[![Code Climate](https://codeclimate.com/github/granthenke/scurator/badges/gpa.svg)](https://codeclimate.com/github/granthenke/scurator)
[![Maven Central](https://img.shields.io/maven-central/v/org.scurator/scurator_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/org.scurator/scurator_2.11)
[![scaladoc](http://javadoc-badge.appspot.com/org.scurator/scurator_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.scurator/scurator_2.11)

Getting Started:
----------------

*Note*: Use artifactId `scurator_2.10` if you want to use with Scala 2.10

```scala
// Add needed imports
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.scurator.SCuratorClient
import org.scurator.components._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

// Create and start a curator client
val curator = CuratorFrameworkFactory.newClient("localhost:2181", new RetryOneTime(1))
curator.start()

// Wrap the instance with SCuratorClient
val client = SCuratorClient(curator)

// Call create on the SCuratorClient
val create = client.create(CreateRequest(path = "/foo"))

// Handle the future response
create.onComplete {
  case Success(response) => println(s"Created node: ${response.path}")
  case Failure(ex) => println(s"Failed: ${ex.getMessage}")
}
```

How To Build:
-------------
>*Note*:
>   This project uses [Gradle](http://www.gradle.org). You must install [Gradle(2.8)](http://www.gradle.org/downloads).
>   If you would rather not install Gradle locally you can use the [Gradle Wrapper](http://www.gradle.org/docs/current/userguide/gradle_wrapper.html) by replacing all references to ```gradle``` with ```gradlew```.

1. Execute ```gradle build```
2. Find the artifact jars in './<sub-project>/build/libs/'

Intellij Project Setup:
-----------------------
1. Execute ```gradle idea```
2. Open project folder in Intellij or open the generated .ipr file

>*Note*:
>   If you have any issues in Intellij a good first troubleshooting step is to execute ```gradle cleanIdea idea```

Eclipse Project Setup:
----------------------
1. Execute ```gradle eclipse```
2. Open the project folder in Eclipse

>*Note*:
>   If you have any issues in Eclipse a good first troubleshooting step is to execute ```gradle cleanEclipse eclipse```

Functional Differences from Apache Curator:
-------------------------------------------
- Creating nodes with no data does not utilize default data (curator defaults data to the local IP address).
- The relevant path is included in all responses

Notable Libraries/Frameworks Used
---------------------------------
- [Bintray](https://github.com/bintray/gradle-bintray-plugin)
- [Curator](https://github.com/apache/curator)
- [Gradle](https://github.com/gradle/gradle)
- [Logback](https://github.com/qos-ch/logback)
- [Scalariform](https://github.com/daniel-trinh/scalariform)
- [ScalaTest](https://github.com/scalatest/scalatest)
- [Scoverage](https://github.com/scoverage/gradle-scoverage)
