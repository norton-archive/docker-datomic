FROM datomic_base

# Setup Datomic
EXPOSE 8080

# Copy helper utility
COPY ./wait-for-init-console.sh /root

ENTRYPOINT ["bin/console", "-p", "8080"]
CMD ["postgres", "datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic"]
