FROM java:openjdk-8-jre

# Copy helper utility
COPY ./wait-for-init-base.sh /root

# Install PostgreSQL
RUN apt-get update -q && \
    apt-get install -q -y \
            -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" \
            curl \
            postgresql-client
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Disables Leiningen warning about running lein as root
ENV LEIN_ROOT 1

# Install Leiningen 2.5.3 and make executable
RUN curl -s https://raw.githubusercontent.com/technomancy/leiningen/2.5.3/bin/lein > \
            /usr/local/bin/lein && \
            chmod 0755 /usr/local/bin/lein && \
            /usr/local/bin/lein

# Install Datomic
ENV DATOMIC_VERSION 0.9.5350
ENV DATOMIC_HOME /opt/datomic-pro-$DATOMIC_VERSION

COPY datomic-pro-$DATOMIC_VERSION.zip /tmp/datomic.zip
RUN unzip /tmp/datomic.zip -d /opt \
  && rm -f /tmp/datomic.zip

# Change work directory
WORKDIR $DATOMIC_HOME
