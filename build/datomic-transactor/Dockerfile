FROM datomic_base

# Setup Datomic
ADD config $DATOMIC_HOME/config
ADD .license-key /tmp/.license-key
RUN sed -i.bak -e '/license\-key=/r/tmp/.license-key' $DATOMIC_HOME/config/sql-transactor.properties \
  && rm -f /tmp/.license-key
EXPOSE 4334

# Copy helper utilities
COPY ./init-transactor.sh /root
COPY ./wait-for-init-transactor.sh /root

ENTRYPOINT ["bin/transactor"]
CMD ["config/sql-transactor.properties"]
