FROM datomic_base

# Setup Datomic
RUN mkdir -p /usr/src/app/store
WORKDIR /usr/src/app
COPY ./jepsen.datomic/project.clj /usr/src/app/
RUN lein deps
COPY ./jepsen.datomic /usr/src/app
RUN lein compile
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

ENTRYPOINT ["/root/wait-for-init-tester.sh"]
CMD ["/root/drop-init-tester.sh"]