.PHONY: all build start stop clean realclean

DATOMIC_VERSION = 0.9.5350

all: start

build: build/datomic-base/datomic-pro-$(DATOMIC_VERSION).zip build/datomic-transactor/.license-key
	docker-compose build

start: build
	docker-compose up -d

stop:
	docker-compose stop

clean: stop
	-docker-compose down

realclean: clean
	@docker rm -v $(shell docker ps -a -q -f status=exited) 2>/dev/null || true
	@docker rmi $(shell docker images -q) 2>/dev/null || true
	@docker rm $(shell docker ps -a -q) 2>/dev/null || true
	@rm -f xpriv/datomic-pro-$(DATOMIC_VERSION).zip
	@rm -f build/datomic-base/datomic-pro-$(DATOMIC_VERSION).zip
	@rm -f build/datomic-transactor/.license-key

priv/datomic-pro-$(DATOMIC_VERSION).zip: priv/.credentials
	curl -u $(shell cat priv/.credentials) -SL https://my.datomic.com/repo/com/datomic/datomic-pro/$(DATOMIC_VERSION)/datomic-pro-$(DATOMIC_VERSION).zip -o $@

build/datomic-base/datomic-pro-$(DATOMIC_VERSION).zip: priv/datomic-pro-$(DATOMIC_VERSION).zip
	cp -f priv/datomic-pro-$(DATOMIC_VERSION).zip $@

build/datomic-transactor/.license-key: priv/.license-key
	cp -f priv/.license-key $@
