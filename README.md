# spring5-react-isomorphic-websockets-mongo-scala
## TL;DR
Please check my original repository, with the code written in Java (except for the client), for details. This one is written in Scala. It was mostly converted to Scala using scalagen. This is for those of you have may be interested in using Scala with Spring, and in particular, with WebFlux. **Disclaimer**: Using java libraries full of lambdas like Reactor in Scala will give you some amount of pain. There is a wrapper library for Reactor I am using, but the ides in Scala aren't at the same level as they are in Java. And yes, this does not use sbt, it uses maven.

## Build steps:
Git clone the repo, cd into the directory and run **mvn compile**
