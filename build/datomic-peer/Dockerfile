FROM datomic_base

# Setup Datomic
EXPOSE 8001

# Copy helper utility
COPY ./wait-for-init-peer.sh /root

ENTRYPOINT ["bin/rest", "-p", "8001"]
CMD ["postgres", "datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic"]
