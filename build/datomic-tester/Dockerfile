FROM datomic_base

# Install Packages
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update -q \
 && apt-get install -q -y \
            -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" \
            maven
RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Setup Datomic
RUN mkdir -p /root/.m2
COPY ./.m2 /root/.m2
WORKDIR /opt/datomic-pro
RUN ./bin/maven-install
RUN mkdir -p /usr/src/app/store
WORKDIR /usr/src/app
COPY ./jepsen.datomic/project.clj /usr/src/app/
RUN lein deps
COPY ./jepsen.datomic /usr/src/app
RUN lein compile
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

ENTRYPOINT ["/root/wait-for-init-tester.sh"]
CMD ["/root/drop-init-tester.sh"]