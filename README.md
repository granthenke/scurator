SCurator
========
An asynchronous Scala wrapper around the Apache Curator Framework.

[![Build Status](https://travis-ci.org/granthenke/scurator.svg)](https://travis-ci.org/granthenke/scurator)
[![codecov.io](http://codecov.io/github/granthenke/scurator/coverage.svg?branch=master)](http://codecov.io/github/granthenke/scurator?branch=master)


Getting Started:
----------------
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
- [Curator](https://github.com/apache/curator)
- [Gradle](https://github.com/gradle/gradle)
- [Logback](https://github.com/qos-ch/logback)
- [Scalariform](https://github.com/daniel-trinh/scalariform)
- [ScalaTest](https://github.com/scalatest/scalatest)
- [Scala-Logging](https://github.com/typesafehub/scala-logging)
- [Scoverage](https://github.com/scoverage/gradle-scoverage)
