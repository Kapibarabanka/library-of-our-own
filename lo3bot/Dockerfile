FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.10.1_3.4.2

WORKDIR /app/lo3common

COPY ../lo3common/build.sbt ./
COPY ../lo3common/src ./src
COPY ../lo3common/project/build.properties ./project/
COPY ../lo3common/project/Dependencies.scala ./project/
COPY ../lo3common/project/plugins.sbt ./project/

WORKDIR /app/lo3bot

COPY ../lo3bot/build.sbt ./
COPY ../lo3bot/src ./src
COPY ../lo3bot/project/build.properties ./project/
COPY ../lo3bot/project/plugins.sbt ./project/

RUN sbt clean compile

ENTRYPOINT ["sbt", "runMain kapibarabanka.lo3.bot.Application"]