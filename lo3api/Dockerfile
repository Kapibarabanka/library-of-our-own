FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.10.1_3.4.2

WORKDIR /app/lo3common

COPY ../lo3common/build.sbt ./
COPY ../lo3common/src ./src
COPY ../lo3common/project/build.properties ./project/
COPY ../lo3common/project/Dependencies.scala ./project/
COPY ../lo3common/project/plugins.sbt ./project/

WORKDIR /app/lo3api

COPY ../lo3api/build.sbt ./
COPY ../lo3api/src ./src
COPY ../lo3api/project/build.properties ./project/
COPY ../lo3api/project/plugins.sbt ./project/

RUN sbt clean compile

ENTRYPOINT ["sbt", "runMain kapibarabanka.lo3.api.Application"]