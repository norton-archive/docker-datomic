FROM datomic_base

# Copy helper utility
COPY ./wait-for-init-tester.sh /root

# Setup Datomic
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY ./jepsen.datomic/project.clj /usr/src/app/
RUN lein deps
COPY ./jepsen.datomic /usr/src/app

# Run the tester
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
CMD ["java", "-jar", "app-standalone.jar"]