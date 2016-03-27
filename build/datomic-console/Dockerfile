FROM datomic_base

# Copy helper utility
COPY ./wait-for-init-console.sh /root

# Setup Datomic
EXPOSE 8080

ENTRYPOINT ["bin/console", "-p", "8080"]
CMD ["postgres", "datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic"]
