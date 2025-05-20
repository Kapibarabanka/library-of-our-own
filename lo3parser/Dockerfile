FROM mcr.microsoft.com/dotnet/sdk:9.0 AS builder
EXPOSE 80

WORKDIR /source

## copy csproj and restore as distinct layers
COPY ./*.fsproj ./
RUN dotnet restore

## copy and publish app and libraries
COPY . .
RUN dotnet build -c Release
RUN dotnet build -c Release
RUN dotnet publish -c Release -r linux-x64 --self-contained true -p:PublishSingleFile=true -p:PublishTrimmed=true -p:PublishReadyToRun=true -o /app

FROM selenium/standalone-chrome:131.0

USER root

# install  Xvfb
RUN apt-get update && apt-get install -y xvfb build-essential libffi-dev && apt-get clean && rm -rf /var/lib/apt/lists/*

# # set Python-related environment variables
# ENV PYTHONUNBUFFERED=1
ENV DISPLAY=:99

# set up the working directory
WORKDIR /app
COPY --from=builder /app .

# ensure correct permissions for /tmp/.X11-unix to prevent Xvfb from issuing warnings
RUN mkdir -p /tmp/.X11-unix && chmod 1777 /tmp/.X11-unix

# # change ownership of venv to seluser and switch users
# RUN chown -R seluser:seluser /opt/venv /app
# USER seluser

# run Xvfb and the Python script
CMD ["sh", "-c", "Xvfb :99 -ac 2>/dev/null & ./lo3parser"]