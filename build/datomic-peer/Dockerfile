FROM datomic_base

# Copy helper utility
COPY ./wait-for-init-peer.sh /root

# Setup Datomic
EXPOSE 8001

ENTRYPOINT ["bin/rest", "-p", "8001"]
CMD ["postgres", "datomic:sql://datomic?jdbc:postgresql://postgres:5432/datomic?user=datomic&password=datomic"]
