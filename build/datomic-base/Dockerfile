FROM java:openjdk-8-jre

# Install Packages
ENV DEBIAN_FRONTEND noninteractive
RUN rm /etc/apt/apt.conf.d/docker-clean
RUN apt-get update -q \
 && apt-get install -q -y \
            -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" \
            apt-utils \
            curl \
            gnuplot \
            openssh-client \
            openssh-server \
            postgresql-client \
            sudo \
            wget \
            sysvinit-core \
            sysvinit \
            sysvinit-utils \
            curl \
            vim \
            man-db \
            faketime \
            unzip \
            iptables \
            iputils-ping \
            rsyslog \
            logrotate
RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN apt-get update -q

# Setup ssh
RUN echo 'root:root' | chpasswd
RUN sed -i.bak 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN mkdir -p /root/.ssh
RUN touch /root/.ssh/authorized_keys
RUN touch /root/.ssh/known_hosts
RUN ssh-keygen -t rsa -N "" -f /root/.ssh/id_rsa
RUN cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys
RUN chmod 600 /root/.ssh/*
EXPOSE 22

# Install Leiningen and make executable
# Disables Leiningen warning about running lein as root
ENV LEIN_VERSION 2.5.3
ENV LEIN_INSTALL /usr/local/bin
ENV LEIN_ROOT 1
RUN curl -s https://raw.githubusercontent.com/technomancy/leiningen/$LEIN_VERSION/bin/lein > \
      $LEIN_INSTALL/lein \
 && chmod 0755 $LEIN_INSTALL/lein \
 && $LEIN_INSTALL/lein

# Install Datomic
ENV DATOMIC_VERSION 0.9.5350
ENV DATOMIC_HOME /opt/datomic-pro

COPY datomic-pro-$DATOMIC_VERSION.zip /tmp/datomic.zip
RUN unzip -qq /tmp/datomic.zip -d /opt \
 && rm -f /tmp/datomic.zip \
 && mv /opt/datomic-pro-$DATOMIC_VERSION $DATOMIC_HOME

# Setup Datomic
ADD config $DATOMIC_HOME/config
ADD .license-key /tmp/.license-key
RUN sed -i.bak -e '/license\-key=/r/tmp/.license-key' $DATOMIC_HOME/config/sql-transactor.properties \
 && rm -f /tmp/.license-key
EXPOSE 4334
EXPOSE 8001
EXPOSE 8080

# Change work directory
WORKDIR $DATOMIC_HOME
