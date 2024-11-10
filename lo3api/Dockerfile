FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.10_7_1.10.1_3.4.2

# Install prerequisites
RUN apt update && \
    apt install -y --no-install-recommends \
                  # Calibre deps
                  ca-certificates \
                  curl \
                  gnupg2 \
                  xz-utils \
                  python3 \
                  # QTWebEngine deps
                  libxdamage-dev libxrandr-dev libxtst6 \
                  # for kindle support
                  xvfb \
                  libegl1 \
                  libopengl0 \
                  libxkbcommon-x11-0 \
                  libxcomposite-dev \
                  # calibre 7
                  libxcb-cursor0 \
                  && rm -rf /var/lib/apt/lists/*

# Install calibre
RUN curl -s https://download.calibre-ebook.com/linux-installer.sh | sh /dev/stdin

# Install EpubMerge
RUN curl -s -O https://plugins.calibre-ebook.com/169744.zip \
    && calibre-customize -a 169744.zip \
    && rm *.zip


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