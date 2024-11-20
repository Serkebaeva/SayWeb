FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR into the container
COPY target/SayWeb-0.0.1-SNAPSHOT.jar /app/SayWeb.jar

# Copy static files (audio, HTML, etc.) to the right location in the container
COPY static /app/static

# Change the APT mirror to a different one and install espeak-ng
RUN sed -i 's|http://deb.debian.org/debian|http://ftp.us.debian.org/debian|' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y espeak-ng --fix-missing && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create the static/audio directory inside the container and Set permissions
RUN mkdir -p /app/static/audio
RUN chmod -R 777 /app/static/audio

ENV AUDIO_DIR=/app/static/audio

# Expose the application port
EXPOSE 3000

# Start the application
ENTRYPOINT ["java", "-jar", "/app/SayWeb.jar"]